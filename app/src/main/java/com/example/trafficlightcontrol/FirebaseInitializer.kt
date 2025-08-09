package com.example.trafficlightcontrol

import com.example.trafficlightcontrol.domain.model.LightPhase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseInitializer @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    suspend fun initializeFirestoreData() {
        try {
            // Initialize traffic light states
            initializeTrafficLightState()

            // Initialize default phase configurations
            initializePhaseConfigurations()
        } catch (e: Exception) {
            // Handle initialization errors
            e.printStackTrace()
        }
    }

    private suspend fun initializeTrafficLightState() {
        // Check if traffic light states exist
        val northSouthRef = firestore.collection("trafficLights").document("north_south")
        val northSouthDoc = northSouthRef.get().await()

        if (!northSouthDoc.exists()) {
            // Create North-South traffic light state
            val northSouthState = mapOf(
                "currentPhase" to LightPhase.RED.name,
                "remainingTime" to 60,
                "isManualOverride" to false
            )

            northSouthRef.set(northSouthState).await()
        }

        val eastWestRef = firestore.collection("trafficLights").document("east_west")
        val eastWestDoc = eastWestRef.get().await()

        if (!eastWestDoc.exists()) {
            // Create East-West traffic light state
            val eastWestState = mapOf(
                "currentPhase" to LightPhase.GREEN.name,
                "remainingTime" to 55,
                "isManualOverride" to false
            )

            eastWestRef.set(eastWestState).await()
        }
    }

    private suspend fun initializePhaseConfigurations() {
        // Check if configurations exist
        val configsRef = firestore.collection("phaseConfigurations")
        val configsSnapshot = configsRef.get().await()

        if (configsSnapshot.isEmpty) {
            // Create default configurations

            // Morning Rush Hour (7:00 - 10:00)
            val morningConfig = mapOf(
                "name" to "Morning Rush Hour",
                "startTime" to "07:00",
                "endTime" to "10:00",
                "redDuration" to 60,
                "yellowDuration" to 5,
                "greenDuration" to 55
            )

            // Mid Day (10:00 - 16:00)
            val midDayConfig = mapOf(
                "name" to "Mid Day",
                "startTime" to "10:00",
                "endTime" to "16:00",
                "redDuration" to 45,
                "yellowDuration" to 5,
                "greenDuration" to 45
            )

            // Evening Rush Hour (16:00 - 19:00)
            val eveningConfig = mapOf(
                "name" to "Evening Rush Hour",
                "startTime" to "16:00",
                "endTime" to "19:00",
                "redDuration" to 60,
                "yellowDuration" to 5,
                "greenDuration" to 55
            )

            // Night (19:00 - 07:00)
            val nightConfig = mapOf(
                "name" to "Night",
                "startTime" to "19:00",
                "endTime" to "07:00",
                "redDuration" to 30,
                "yellowDuration" to 5,
                "greenDuration" to 30
            )

            // Add configurations to Firestore
            configsRef.add(morningConfig).await()
            configsRef.add(midDayConfig).await()
            configsRef.add(eveningConfig).await()
            configsRef.add(nightConfig).await()
        }
    }
}