package com.example.trafficlightcontrol.data.repoImpl

import com.example.trafficlightcontrol.data.model.PhaseConfigurationDto
import com.example.trafficlightcontrol.data.model.TrafficLightStateDto
import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.domain.model.PhaseConfiguration
import com.example.trafficlightcontrol.domain.model.TimeRange
import com.example.trafficlightcontrol.domain.model.TrafficLightState
import com.example.trafficlightcontrol.domain.repo.TrafficLightRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrafficLightRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TrafficLightRepository {

    override fun observeTrafficLightState(direction: Direction): Flow<TrafficLightState> = callbackFlow {
        val directionStr = direction.name.lowercase()
        val listener = firestore.collection("trafficLights")
            .document(directionStr)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val dto = snapshot.toObject(TrafficLightStateDto::class.java)
                    if (dto != null) {
                        val state = TrafficLightState(
                            id = snapshot.id,
                            direction = direction,
                            currentPhase = dto.currentPhase,
                            remainingTime = dto.remainingTime,
                            isManualOverride = dto.isManualOverride
                        )
                        trySend(state)
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun changePhase(direction: Direction, phase: LightPhase): Result<Unit> = runCatching {
        val directionStr = direction.name.lowercase()

        firestore.collection("trafficLights")
            .document(directionStr)
            .update(
                mapOf(
                    "currentPhase" to phase,
                    "isManualOverride" to true
                )
            ).await()
    }

    override suspend fun resetPhaseTimer(direction: Direction): Result<Unit> = runCatching {
        val directionStr = direction.name.lowercase()

        // Get the current configuration
        val snapshot = firestore.collection("trafficLights")
            .document(directionStr)
            .get()
            .await()

        val state = snapshot.toObject(TrafficLightStateDto::class.java)
            ?: throw Exception("Traffic light state not found")

        // Reset timer based on the current phase
        val duration = when (state.currentPhase) {
            LightPhase.RED -> 60 // Default values, in a real app, get from config
            LightPhase.YELLOW -> 5
            LightPhase.GREEN -> 55
        }

        firestore.collection("trafficLights")
            .document(directionStr)
            .update("remainingTime", duration)
            .await()
    }

    override suspend fun setManualOverride(direction: Direction, override: Boolean): Result<Unit> = runCatching {
        val directionStr = direction.name.lowercase()

        firestore.collection("trafficLights")
            .document(directionStr)
            .update("isManualOverride", override)
            .await()
    }

    override suspend fun getPhaseConfigurations(): Result<List<PhaseConfiguration>> = runCatching {
        val snapshot = firestore.collection("phaseConfigurations")
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            val dto = doc.toObject(PhaseConfigurationDto::class.java) ?: return@mapNotNull null

            val formatter = DateTimeFormatter.ISO_LOCAL_TIME
            val startTime = LocalTime.parse(dto.startTime, formatter)
            val endTime = LocalTime.parse(dto.endTime, formatter)

            PhaseConfiguration(
                id = doc.id,
                timeRange = TimeRange(startTime, endTime),
                redDuration = dto.redDuration,
                yellowDuration = dto.yellowDuration,
                greenDuration = dto.greenDuration,
                name = dto.name
            )
        }
    }

    override suspend fun savePhaseConfiguration(config: PhaseConfiguration): Result<Unit> = runCatching {
        val formatter = DateTimeFormatter.ISO_LOCAL_TIME

        val dto = PhaseConfigurationDto(
            startTime = config.timeRange.start.format(formatter),
            endTime = config.timeRange.end.format(formatter),
            redDuration = config.redDuration,
            yellowDuration = config.yellowDuration,
            greenDuration = config.greenDuration,
            name = config.name
        )

        if (config.id.isNotEmpty()) {
            firestore.collection("phaseConfigurations")
                .document(config.id)
                .set(dto)
                .await()
        } else {
            firestore.collection("phaseConfigurations")
                .add(dto)
                .await()
        }
    }

    override suspend fun deletePhaseConfiguration(configId: String): Result<Unit> = runCatching {
        firestore.collection("phaseConfigurations")
            .document(configId)
            .delete()
            .await()
    }
}