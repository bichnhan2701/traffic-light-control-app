package com.example.trafficlightcontrol.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trafficlightcontrol.ui.component.FormattedDateTime
import com.example.trafficlightcontrol.ui.theme.*
import com.example.trafficlightcontrol.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    username: String
) {
    val systemStatus by viewModel.systemStatus.collectAsState()
    val error by viewModel.error.collectAsState()

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
                title = { Text("Cài đặt") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Phần thông tin người dùng
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Thông tin người dùng",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Blue
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "Tên đăng nhập: $username",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phần đồng bộ thời gian
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Đồng bộ thời gian",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    systemStatus?.let { status ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Blue
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text("Đồng bộ lần cuối:")
                                FormattedDateTime(timestamp = status.sync_time)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = { viewModel.syncTime() },
                        colors = ButtonDefaults.buttonColors(containerColor = Blue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Đồng bộ thời gian ngay",
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phần hướng dẫn sử dụng
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Hướng dẫn sử dụng",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HelpSection(
                        title = "Chế độ theo lịch",
                        description = "Hệ thống tự động chạy theo lịch đã cài đặt, thay đổi chu kỳ và chế độ theo từng khung giờ trong ngày."
                    )

                    HelpSection(
                        title = "Chế độ cao điểm",
                        description = "Ưu tiên kéo dài pha xanh cho hướng có lưu lượng lớn, giảm tần suất dừng xe. Kích hoạt khi lưu lượng giao thông tăng cao."
                    )

                    HelpSection(
                        title = "Chế độ đêm",
                        description = "Tất cả đèn vàng nhấp nháy với chu kỳ 1s (ON/OFF). Chế độ này thường hoạt động từ 22:00 đến 05:00."
                    )

                    HelpSection(
                        title = "Chế độ khẩn cấp",
                        description = "Ưu tiên hoàn toàn cho một hướng giao thông cụ thể. Kích hoạt khi có xe ưu tiên hoặc tình huống khẩn cấp."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phần thông tin phiên bản
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Thông tin ứng dụng",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Phiên bản: 1.0.0")
                    Text("Hệ thống điều khiển đèn giao thông")
                }
            }
        }
    }
}

@Composable
fun HelpSection(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}