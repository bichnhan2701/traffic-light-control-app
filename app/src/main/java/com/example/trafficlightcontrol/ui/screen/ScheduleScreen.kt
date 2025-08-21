package com.example.trafficlightcontrol.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trafficlightcontrol.data.model.ScheduleEntry
import com.example.trafficlightcontrol.ui.component.ModeStatusBadge
import com.example.trafficlightcontrol.ui.theme.*
import com.example.trafficlightcontrol.ui.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    isAdmin: Boolean = true // Giả định người dùng có quyền admin
) {
    val scheduleEntries by viewModel.scheduleEntries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentEditingEntry by viewModel.currentEditingEntry.collectAsState()

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
                        "Chu kỳ mặc định",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    if (isAdmin) {
                        // Nút thêm lịch mới
                        IconButton(
                            onClick = {
                                viewModel.startEditing(
                                    ScheduleEntry(
                                        start_time = "06:00",
                                        end_time = "09:00",
                                        mode = "scheduled",
                                        cycle_time = 60,
                                        green_time_a = 25,
                                        green_time_b = 25
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Thêm lịch")
                        }
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
//                    // Tiêu đề bảng
//                    ScheduleTableHeader()

                    // Danh sách lịch
                    LazyColumn (
                        modifier = Modifier
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(scheduleEntries) { entry ->
                            ScheduleEntryItem(
                                entry = entry,
                                isAdmin = isAdmin,
                                onEdit = { viewModel.startEditing(entry) },
                                onDelete = { viewModel.deleteScheduleEntry(entry.id) },
                                active = "Hoạt động"
                            )
                        }
                    }
                }
            }

            // Dialog chỉnh sửa lịch
            if (currentEditingEntry != null) {
                ScheduleEditDialog(
                    scheduleEntry = currentEditingEntry!!,
                    onSave = { viewModel.saveScheduleEntry(it) },
                    onCancel = { viewModel.cancelEditing() }
                )
            }
        }
    }
}

@Composable
fun ScheduleEntryItem(
    entry: ScheduleEntry,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    active: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ){
                Text(
                    text = "${entry.start_time} - ${entry.end_time}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight =  FontWeight.Bold,
                )

                ModeStatusBadge(
                    mode = entry.mode
                )
            }

            if (isAdmin) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa lịch",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Chu kỳ",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "A: ${entry.green_time_a}s, B: ${entry.green_time_b}s",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Pha A/B",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${entry.green_time_a}s, ${entry.green_time_b}s",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color = Green, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = active,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleEditDialog(
    scheduleEntry: ScheduleEntry,
    onSave: (ScheduleEntry) -> Unit,
    onCancel: () -> Unit
) {
    var startTime by remember { mutableStateOf(scheduleEntry.start_time) }
    var endTime by remember { mutableStateOf(scheduleEntry.end_time) }
    var mode by remember { mutableStateOf(scheduleEntry.mode) }
    var cycleTime by remember { mutableStateOf(scheduleEntry.cycle_time.toString()) }
    var greenTimeA by remember { mutableStateOf(scheduleEntry.green_time_a.toString()) }
    var greenTimeB by remember { mutableStateOf(scheduleEntry.green_time_b.toString()) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (scheduleEntry.id.isEmpty()) "Thêm lịch mới" else "Chỉnh sửa lịch") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                // Thời gian bắt đầu
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Thời gian bắt đầu (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Thời gian kết thúc
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Thời gian kết thúc (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chế độ
                Text("Chọn chế độ:", style = MaterialTheme.typography.titleMedium)

                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = mode == "scheduled",
                        onClick = { mode = "scheduled" }
                    )
                    Text(
                        "Theo lịch",
                        modifier = Modifier
                            .clickable { mode = "scheduled" }
                            .padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = mode == "peak",
                        onClick = { mode = "peak" }
                    )
                    Text(
                        "Cao điểm",
                        modifier = Modifier
                            .clickable { mode = "peak" }
                            .padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = mode == "night",
                        onClick = { mode = "night" }
                    )
                    Text(
                        "Đêm",
                        modifier = Modifier
                            .clickable { mode = "night" }
                            .padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chu kỳ đèn
                OutlinedTextField(
                    value = cycleTime,
                    onValueChange = { cycleTime = it },
                    label = { Text("Chu kỳ đèn (giây)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Chỉ hiển thị thời gian xanh cho A và B nếu không phải chế độ đêm
                if (mode != "night") {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Thời gian xanh cho A
                    OutlinedTextField(
                        value = greenTimeA,
                        onValueChange = { greenTimeA = it },
                        label = { Text("Thời gian xanh hướng A (giây)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Thời gian xanh cho B
                    OutlinedTextField(
                        value = greenTimeB,
                        onValueChange = { greenTimeB = it },
                        label = { Text("Thời gian xanh hướng B (giây)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ScheduleEntry(
                            id = scheduleEntry.id,
                            start_time = startTime,
                            end_time = endTime,
                            mode = mode,
                            cycle_time = cycleTime.toIntOrNull() ?: 60,
                            green_time_a = greenTimeA.toIntOrNull() ?: 25,
                            green_time_b = greenTimeB.toIntOrNull() ?: 25
                        )
                    )
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text("Hủy")
            }
        }
    )
}