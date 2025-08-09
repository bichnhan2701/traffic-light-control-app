package com.example.trafficlightcontrol.data.repoImpl

import android.util.Log
import com.example.trafficlightcontrol.data.model.UserDto
import com.example.trafficlightcontrol.domain.model.User
import com.example.trafficlightcontrol.domain.model.UserRole
import com.example.trafficlightcontrol.domain.repo.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        Log.d("AuthDebug", "Login attempt: $email")
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Authentication failed")
            Log.d("AuthDebug", "Login successful: UID=$userId")

            val userDoc = firestore.collection("users").document(userId).get().await()
            var userDto = userDoc.toObject(UserDto::class.java)

            if (userDto == null) {
                Log.w("AuthDebug", "User document not found in Firestore. Creating default user document.")

                // Determine the appropriate role - use ADMIN for admin@example.com
                val role = if (email == "admin@example.com") {
                    Log.d("AuthDebug", "Admin email detected, assigning ADMIN role")
                    UserRole.ADMIN
                } else {
                    UserRole.OPERATOR
                }

                // Create a default user document for this authenticated user
                userDto = UserDto(
                    email = email,
                    name = email.substringBefore('@'), // Default name from email
                    role = role, // Use determined role
                    isActive = true
                )

                // Save the new user document to Firestore
                firestore.collection("users").document(userId).set(userDto).await()
                Log.d("AuthDebug", "Default user document created for: $userId with role: $role")
            } else if (email == "admin@example.com" && userDto.role != UserRole.ADMIN) {
                // Fix incorrect role for admin account
                Log.d("AuthDebug", "Admin account found with incorrect role: ${userDto.role}. Updating to ADMIN role.")
                userDto = userDto.copy(role = UserRole.ADMIN)
                firestore.collection("users").document(userId).update("role", UserRole.ADMIN).await()
                Log.d("AuthDebug", "Admin role updated successfully")
            }

            Log.d("AuthDebug", "User data fetched: role=${userDto.role}, active=${userDto.isActive}")

            User(
                id = userId,
                email = userDto.email,
                name = userDto.name,
                role = userDto.role,
                isActive = userDto.isActive
            )
        } catch (e: Exception) {
            Log.e("AuthDebug", "Login failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        val currentUser = auth.currentUser
        Log.d("AuthDebug", "Logout attempt: ${currentUser?.email ?: "No user logged in"}")
        auth.signOut()
        Log.d("AuthDebug", "User signed out successfully")
    }

    override suspend fun getCurrentUser(): Flow<User?> = callbackFlow {
        Log.d("AuthDebug", "Setting up authentication state listener")
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                Log.d("AuthDebug", "Auth state changed: User logged in (${firebaseUser.email}, ID: ${firebaseUser.uid})")
                // Launch a coroutine to fetch user data
                launch {
                    try {
                        val userDoc = firestore.collection("users")
                            .document(firebaseUser.uid)
                            .get().await()

                        val userDto = userDoc.toObject(UserDto::class.java)
                        if (userDto != null) {
                            Log.d("AuthDebug", "User data fetched: ${userDto.email}, role=${userDto.role}, active=${userDto.isActive}")
                            val user = User(
                                id = firebaseUser.uid,
                                email = userDto.email,
                                name = userDto.name,
                                role = userDto.role,
                                isActive = userDto.isActive
                            )
                            trySend(user)
                        } else {
                            Log.w("AuthDebug", "User authenticated but no data found in Firestore for ID: ${firebaseUser.uid}")
                            trySend(null)
                        }
                    } catch (e: Exception) {
                        Log.e("AuthDebug", "Error fetching user data: ${e.message}", e)
                        trySend(null)
                    }
                }
            } else {
                Log.d("AuthDebug", "Auth state changed: No user logged in")
                trySend(null)
            }
        }

        auth.addAuthStateListener(listener)
        awaitClose {
            Log.d("AuthDebug", "Removing authentication state listener")
            auth.removeAuthStateListener(listener)
        }
    }

    override suspend fun createUser(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Result<User> = runCatching {
        Log.d("AuthDebug", "Creating new user account: $email, role: $role")
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User creation failed")
            Log.d("AuthDebug", "Firebase Auth account created successfully: UID=$userId")

            val userDto = UserDto(
                email = email,
                name = name,
                role = role,
                isActive = true
            )

            firestore.collection("users").document(userId).set(userDto).await()
            Log.d("AuthDebug", "User data saved to Firestore: $userId")

            User(
                id = userId,
                email = userDto.email,
                name = userDto.name,
                role = userDto.role,
                isActive = userDto.isActive
            )
        } catch (e: Exception) {
            Log.e("AuthDebug", "User creation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> = runCatching {
        val userDto = UserDto(
            email = user.email,
            name = user.name,
            role = user.role,
            isActive = user.isActive
        )

        firestore.collection("users").document(user.id).set(userDto).await()
    }

    override suspend fun deleteUser(userId: String): Result<Unit> = runCatching {
        // Note: In a real app, consider soft deletion or additional security checks
        firestore.collection("users").document(userId).delete().await()

        // Optionally, delete the Firebase Auth user as well
        // This would require admin SDK or Firebase Functions
    }

    override suspend fun getAllUsers(): Result<List<User>> = runCatching {
        val snapshot = firestore.collection("users").get().await()
        snapshot.documents.mapNotNull { doc ->
            val userDto = doc.toObject(UserDto::class.java) ?: return@mapNotNull null
            User(
                id = doc.id,
                email = userDto.email,
                name = userDto.name,
                role = userDto.role,
                isActive = userDto.isActive
            )
        }
    }

    override suspend fun getUserById(userId: String): Result<User> = runCatching {
        val userDoc = firestore.collection("users").document(userId).get().await()
        val userDto = userDoc.toObject(UserDto::class.java)
            ?: throw Exception("User not found")

        User(
            id = userId,
            email = userDto.email,
            name = userDto.name,
            role = userDto.role,
            isActive = userDto.isActive
        )
    }
}