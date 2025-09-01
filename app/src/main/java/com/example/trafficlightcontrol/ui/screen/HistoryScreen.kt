package com.example.trafficlightcontrol.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trafficlightcontrol.data.model.LogEntry
import com.example.trafficlightcontrol.data.model.LogType
import com.example.trafficlightcontrol.data.model.Mode
import com.example.trafficlightcontrol.ui.theme.*
import com.example.trafficlightcontrol.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val uiState by viewModel.state.collectAsState()
    val scaffoldState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = scaffoldState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch sử hoạt động",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(Modifier.fillMaxSize()) {
                    if (uiState.dateFilter != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FilterAlt, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Lọc theo ngày: ${uiState.dateFilter}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearFilter() }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa bộ lọc", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    val grouped: List<Pair<String, List<LogEntry>>> = remember(uiState.items) {
                        val sorted = uiState.items.sortedByDescending { it.ts }
                        sorted.groupBy { dateFormat.format(Date(it.ts)) }
                            .toList()
                    }

                    if (grouped.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Không có hoạt động nào")
                        }
                    } else {
                        LazyColumn(Modifier.padding(16.dp)) {
                            grouped.forEach { (dateStr, logs) ->
                                item { LogListHeader(dateStr) }
                                items(logs) { entry ->
                                    LogEntryItem(entry, timeText = timeFormat.format(Date(entry.ts)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogListHeader(dateStr: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 8.dp, top = 12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = dateStr,
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}


@Composable
fun LogEntryItem(entry: LogEntry, timeText: String) {
    val (actionName, actionIcon, actionColor) = actionMeta(entry)
    val description = buildDescription(entry) // hàm thuần, giữ nguyên

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(12.dp).background(actionColor, CircleShape))
            Box(
                Modifier
                    .size(width = 2.dp, height = 35.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant, RectangleShape)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(actionIcon, null, tint = actionColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = actionName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = actionColor
                        )
                    }
                    Text(
                        timeText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = description,
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

/** Màu + icon + nhãn theo Mode + LogType (đã tách emergency_A/B) */
@Composable
private fun actionMeta(entry: LogEntry): Triple<String, ImageVector, Color> {
    val mode = entry.mode
    val (name, icon) = when (entry.type) {
        LogType.MODE_START -> when (mode) {
            Mode.night        -> "Bật chế độ đêm" to Icons.Default.Schedule
            Mode.emergency_A  -> "Bật khẩn cấp (A)" to Icons.Default.Warning
            Mode.emergency_B  -> "Bật khẩn cấp (B)" to Icons.Default.Warning
            Mode.peak         -> "Bật chế độ giờ cao điểm" to Icons.Default.TrendingUp
            Mode.default      -> "Bật chế độ mặc định" to Icons.Default.CheckCircle
            null              -> "Bắt đầu" to Icons.Default.CheckCircle
        }
        LogType.MODE_END -> when (mode) {
            Mode.night        -> "Kết thúc chế độ đêm" to Icons.Default.StopCircle
            Mode.emergency_A  -> "Kết thúc khẩn cấp (A)" to Icons.Default.StopCircle
            Mode.emergency_B  -> "Kết thúc khẩn cấp (B)" to Icons.Default.StopCircle
            Mode.peak         -> "Kết thúc giờ cao điểm" to Icons.Default.StopCircle
            Mode.default      -> "Kết thúc chế độ mặc định" to Icons.Default.StopCircle
            null              -> "Kết thúc" to Icons.Default.StopCircle
        }
        else -> "Sự kiện" to Icons.Default.CheckCircle
    }

    val color = when (mode) {
        Mode.night        -> Color.DarkGray
        Mode.emergency_A,
        Mode.emergency_B  -> Red
        Mode.peak         -> Orange
        Mode.default, null -> MaterialTheme.colorScheme.primary
    }
    return Triple(name, icon, color)
}

/** Mô tả chi tiết (peak hiển thị A/B sec; emergency A/B hiển thị hướng) */
private fun buildDescription(entry: LogEntry): String {
    return when (entry.mode) {
        Mode.emergency_A ->
            if (entry.type == LogType.MODE_START) "Ưu tiên hướng A" else "Kết thúc khẩn cấp (A)"
        Mode.emergency_B ->
            if (entry.type == LogType.MODE_START) "Ưu tiên hướng B" else "Kết thúc khẩn cấp (B)"
        Mode.peak ->
            if (entry.type == LogType.MODE_START)
                "A: ${entry.greenA_s ?: "?"}s • B: ${entry.greenB_s ?: "?"}s"
            else "Kết thúc chế độ giờ cao điểm"
        Mode.night ->
            if (entry.type == LogType.MODE_START) "Nhấp nháy vàng" else "Kết thúc chế độ đêm"
        Mode.default ->
            if (entry.type == LogType.MODE_START) "Về lịch mặc định" else "Tạm dừng chế độ mặc định"
        null -> "Sự kiện không xác định"
    }
}