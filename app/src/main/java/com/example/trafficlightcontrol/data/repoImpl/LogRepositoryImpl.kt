package com.example.trafficlightcontrol.data.repoImpl

import com.example.trafficlightcontrol.data.model.ActionLogDto
import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LogRepository {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override suspend fun addLog(log: ActionLog): Result<Unit> = runCatching {
        val dto = ActionLogDto(
            userId = log.userId,
            userName = log.userName,
            userRole = log.userRole,
            timestamp = log.timestamp.format(formatter),
            actionType = log.actionType,
            description = log.description,
            previousState = log.previousState,
            newState = log.newState
        )

        firestore.collection("logs")
            .add(dto)
            .await()
    }

    override suspend fun getLogsByUser(userId: String): Result<List<ActionLog>> = runCatching {
        val snapshot = firestore.collection("logs")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        mapLogDocumentsToLogs(snapshot.documents)
    }

    override suspend fun getLogsByTimeRange(
        start: LocalDateTime,
        end: LocalDateTime
    ): Result<List<ActionLog>> = runCatching {
        val startStr = start.format(formatter)
        val endStr = end.format(formatter)

        val snapshot = firestore.collection("logs")
            .whereGreaterThanOrEqualTo("timestamp", startStr)
            .whereLessThanOrEqualTo("timestamp", endStr)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        mapLogDocumentsToLogs(snapshot.documents)
    }

    override suspend fun getLogsByAction(actionType: ActionType): Result<List<ActionLog>> = runCatching {
        val snapshot = firestore.collection("logs")
            .whereEqualTo("actionType", actionType)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        mapLogDocumentsToLogs(snapshot.documents)
    }

    override fun observeRecentLogs(): Flow<List<ActionLog>> = callbackFlow {
        val listener = firestore.collection("logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val logs = mapLogDocumentsToLogs(snapshot.documents)
                    trySend(logs)
                }
            }

        awaitClose { listener.remove() }
    }

    private fun mapLogDocumentsToLogs(documents: List<com.google.firebase.firestore.DocumentSnapshot>): List<ActionLog> {
        return documents.mapNotNull { doc ->
            val dto = doc.toObject(ActionLogDto::class.java) ?: return@mapNotNull null

            val timestamp = LocalDateTime.parse(dto.timestamp, formatter)

            ActionLog(
                id = doc.id,
                userId = dto.userId,
                userName = dto.userName,
                userRole = dto.userRole,
                timestamp = timestamp,
                actionType = dto.actionType,
                description = dto.description,
                previousState = dto.previousState,
                newState = dto.newState
            )
        }
    }
}