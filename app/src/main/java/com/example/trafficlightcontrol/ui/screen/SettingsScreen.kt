package com.example.trafficlightcontrol.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trafficlightcontrol.data.model.Durations
import com.example.trafficlightcontrol.data.model.Mode
import com.example.trafficlightcontrol.ui.component.PhaseConfigCard
import com.example.trafficlightcontrol.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
) {
    val ui by viewModel.uiState.collectAsState()
    val p = ui.phasePanel
    val scaffoldState = remember { SnackbarHostState() }

    // Hiển thị Snackbar khi có lỗi
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = scaffoldState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cài đặt",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Thông tin pha mặc định",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(12.dp))
            PhaseConfigCard(
                mode = p?.mode ?: Mode.default,
                durations = ui.durations ?: Durations(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Phần hướng dẫn sử dụng
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HelpSection(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}