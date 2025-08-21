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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trafficlightcontrol.data.model.LogEntry
import com.example.trafficlightcontrol.ui.component.FormattedDate
import com.example.trafficlightcontrol.ui.component.FormattedTime
import com.example.trafficlightcontrol.ui.theme.*
import com.example.trafficlightcontrol.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val logs by viewModel.logs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val scaffoldState = remember { SnackbarHostState() }

    // Hiển thị Snackbar khi có lỗi
    LaunchedEffect(error) {
        error?.let {
            scaffoldState.showSnackbar(
                message = it,
                actionLabel = "Đóng"
            )
            viewModel.clearError()
        }
    }

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
                actions = {
                    // Nút chọn ngày
                    IconButton(onClick = {
                        // Bỏ bộ lọc ngày
                        if (selectedDate != null) {
                            viewModel.setDateFilter(null)
                        } else {
                            // Chọn ngày hiện tại để lọc
                            viewModel.setDateFilter(Date())
                        }
                    }) {
                        Icon(
                            if (selectedDate == null) Icons.Default.CalendarToday
                            else Icons.Default.FilterAlt,
                            contentDescription = "Chọn ngày"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Hiển thị bộ lọc đang áp dụng
                    if (selectedDate != null) {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Blue.copy(alpha = 0.1f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = Blue
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                "Lọc theo ngày: ${dateFormat.format(selectedDate!!)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Blue
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = { viewModel.setDateFilter(null) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Xóa bộ lọc",
                                    tint = Blue
                                )
                            }
                        }
                    }



                    // Danh sách log
                    if (logs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (selectedDate != null)
                                    "Không có hoạt động nào trong ngày đã chọn"
                                else
                                    "Không có lịch sử hoạt động"
                            )
                        }
                    } else {
                        // Group logs by date
                        val groupedLogs = logs.groupBy { log ->
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(log.timestamp)
                        }
                        LazyColumn(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            groupedLogs.forEach { (date, logEntries) ->
                                // Show header for each date group
                                item {
                                    LogListHeader(logEntries.first())
                                }
                                items(logEntries) { logEntry ->
                                    LogEntryItem(logEntry)
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
fun LogListHeader(entry: LogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp, top = 12.dp),
        horizontalArrangement =  Arrangement.End,
    ) {
        FormattedDate(timestamp = entry.timestamp)
    }
}

@Composable
fun LogEntryItem(entry: LogEntry) {
    val actionColor = when {
        entry.action.contains("emergency") -> Color.Red
        entry.action.contains("mode_change") -> Blue
        else -> Color.Black
    }

    val actionIcon = when {
        entry.action.contains("emergency") -> Icons.Default.Warning
        entry.action.contains("mode_change") -> Icons.Default.Sync
        else -> Icons.Default.Info
    }

    val actionName = when {
        entry.action.contains("emergency") -> "Khẩn cấp"
        entry.action.contains("mode_change") -> "Chuyển đổi chế độ"
        else -> "Thông tin"
    }

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column (
            verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(actionColor, CircleShape)
            )
            Box (
                modifier = Modifier
                    .size(width = 2.dp, height = 35.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant, RectangleShape)
            )

        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
        ) {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = null,
                            tint = actionColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = actionName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = actionColor
                        )
                    }
                    FormattedTime(timestamp = entry.timestamp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text (
                        text = getActionDescription(entry.action, entry.from, entry.to),
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyMedium
                            .copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp,
                            )
                    )
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cột người thực hiện
                    Text(
                        text = "Bởi: ${entry.by}",
                        modifier = Modifier.weight(1.5f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text (
                        text = "2 phút trước",
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Hàm hiển thị mô tả hành động dễ đọc
fun getActionDescription(action: String, from: String, to: String): String {
    return when (action) {
        "mode_change" -> "Chuyển chế độ từ $from sang $to"
        "emergency_activated" -> "Kích hoạt khẩn cấp ${to.replace("emergency_", "")}"
        "emergency_deactivated" -> "Kết thúc khẩn cấp"
        else -> action
    }
}