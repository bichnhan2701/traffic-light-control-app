package com.example.trafficlightcontrol.domain.repo

import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface LogRepository {
    suspend fun addLog(log: ActionLog): Result<Unit>
    suspend fun getLogsByUser(userId: String): Result<List<ActionLog>>
    suspend fun getLogsByTimeRange(start: LocalDateTime, end: LocalDateTime): Result<List<ActionLog>>
    suspend fun getLogsByAction(actionType: ActionType): Result<List<ActionLog>>
    fun observeRecentLogs(): Flow<List<ActionLog>>
}