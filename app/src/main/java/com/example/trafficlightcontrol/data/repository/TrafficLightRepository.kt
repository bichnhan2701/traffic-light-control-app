package com.example.trafficlightcontrol.data.repository

import com.example.trafficlightcontrol.data.model.EmergencyStatus
import com.example.trafficlightcontrol.data.model.LogEntry
import com.example.trafficlightcontrol.data.model.ScheduleEntry
import com.example.trafficlightcontrol.data.model.SystemStatus
import com.example.trafficlightcontrol.data.model.TrafficLightCurrent
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TrafficLightRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentRef = database.getReference("traffic_light/current")
    private val scheduleRef = database.getReference("traffic_light/schedule")
    private val emergencyRef = database.getReference("traffic_light/emergency")
    private val logRef = database.getReference("traffic_light/log")
    private val systemRef = database.getReference("traffic_light/system")

    // Lắng nghe trạng thái hiện tại
    fun getCurrentStatus(): Flow<TrafficLightCurrent> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val current = snapshot.getValue(TrafficLightCurrent::class.java)
                if (current != null) {
                    trySend(current)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        }

        currentRef.addValueEventListener(listener)

        // Cleanup khi Flow bị hủy
        awaitClose {
            currentRef.removeEventListener(listener)
        }
    }

    // Lắng nghe lịch trình
    fun getSchedule(): Flow<List<ScheduleEntry>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val scheduleEntries = mutableListOf<ScheduleEntry>()
                for (childSnapshot in snapshot.children) {
                    val entry = childSnapshot.getValue(ScheduleEntry::class.java)?.copy(id = childSnapshot.key ?: "")
                    if (entry != null) {
                        scheduleEntries.add(entry)
                    }
                }
                trySend(scheduleEntries)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        }

        scheduleRef.addValueEventListener(listener)

        awaitClose {
            scheduleRef.removeEventListener(listener)
        }
    }

    // Lắng nghe trạng thái khẩn cấp
    fun getEmergencyStatus(): Flow<EmergencyStatus> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emergency = snapshot.getValue(EmergencyStatus::class.java)
                if (emergency != null) {
                    trySend(emergency)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        }

        emergencyRef.addValueEventListener(listener)

        awaitClose {
            emergencyRef.removeEventListener(listener)
        }
    }

    // Lấy log lịch sử
    fun getLogs(limit: Int = 50): Flow<List<LogEntry>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logs = mutableListOf<LogEntry>()
                for (childSnapshot in snapshot.children) {
                    val log = childSnapshot.getValue(LogEntry::class.java)?.copy(id = childSnapshot.key ?: "")
                    if (log != null) {
                        logs.add(log)
                    }
                }
                trySend(logs.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        }

        logRef.orderByChild("timestamp").limitToLast(limit).addValueEventListener(listener)

        awaitClose {
            logRef.removeEventListener(listener)
        }
    }

    // Lắng nghe trạng thái hệ thống
    fun getSystemStatus(): Flow<SystemStatus> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val system = snapshot.getValue(SystemStatus::class.java)
                if (system != null) {
                    trySend(system)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        }

        systemRef.addValueEventListener(listener)

        awaitClose {
            systemRef.removeEventListener(listener)
        }
    }

    // Thay đổi chế độ hoạt động
    suspend fun changeMode(mode: String, username: String) {
        val updates = hashMapOf<String, Any>(
            "mode" to mode,
            "last_updated_by" to username,
            "timestamp" to ServerValue.TIMESTAMP
        )
        currentRef.updateChildren(updates).await()

        // Ghi log
        val logEntry = hashMapOf(
            "action" to "mode_change",
            "to" to mode,
            "by" to username,
            "timestamp" to ServerValue.TIMESTAMP
        )
        logRef.push().setValue(logEntry).await()
    }

    // Kích hoạt chế độ khẩn cấp
    suspend fun activateEmergency(direction: String, username: String) {
        val emergencyData = hashMapOf(
            "active" to true,
            "priority_direction" to direction,
            "activated_by" to username,
            "timestamp" to ServerValue.TIMESTAMP
        )
        emergencyRef.setValue(emergencyData).await()

        // Ghi log
        val logEntry = hashMapOf(
            "action" to "emergency_activated",
            "to" to "emergency_$direction",
            "by" to username,
            "timestamp" to ServerValue.TIMESTAMP
        )
        logRef.push().setValue(logEntry).await()
    }

    // Kết thúc chế độ khẩn cấp
    suspend fun deactivateEmergency(username: String) {
        val emergencyData = hashMapOf(
            "active" to false,
            "timestamp" to ServerValue.TIMESTAMP
        )
        emergencyRef.updateChildren(emergencyData).await()

        // Ghi log
        val logEntry = hashMapOf(
            "action" to "emergency_deactivated",
            "by" to username,
            "timestamp" to ServerValue.TIMESTAMP
        )
        logRef.push().setValue(logEntry).await()
    }

    // Thêm lịch mới
    suspend fun addScheduleEntry(entry: ScheduleEntry) {
        scheduleRef.push().setValue(entry).await()
    }

    // Cập nhật lịch
    suspend fun updateScheduleEntry(entry: ScheduleEntry) {
        val updates = hashMapOf<String, Any>(
            "start_time" to entry.start_time,
            "end_time" to entry.end_time,
            "mode" to entry.mode,
            "cycle_time" to entry.cycle_time,
            "green_time_a" to entry.green_time_a,
            "green_time_b" to entry.green_time_b
        )
        scheduleRef.child(entry.id).updateChildren(updates).await()
    }

    // Xóa lịch
    suspend fun deleteScheduleEntry(id: String) {
        scheduleRef.child(id).removeValue().await()
    }

    // Đồng bộ thời gian
    suspend fun syncTime() {
        systemRef.child("sync_time").setValue(ServerValue.TIMESTAMP).await()
    }
}