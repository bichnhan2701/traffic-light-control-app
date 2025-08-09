package com.example.trafficlightcontrol.domain.repo

import com.example.trafficlightcontrol.domain.model.User
import com.example.trafficlightcontrol.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Flow<User?>
    suspend fun createUser(email: String, password: String, name: String, role: UserRole): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun getAllUsers(): Result<List<User>>
    suspend fun getUserById(userId: String): Result<User>
}