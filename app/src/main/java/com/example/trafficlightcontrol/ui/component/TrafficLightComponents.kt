package com.example.trafficlightcontrol.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trafficlightcontrol.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.trafficlightcontrol.data.model.*

@Composable
fun ModeCard(
    title: String,
    color: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color.copy(alpha = 0.2f), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) { icon() }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                        fontSize = 16.sp
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Go",
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


@Composable
fun TrafficLightDisplay(
    modifier: Modifier = Modifier,
    mode: Mode,
    phase: Phase,
    timeLeftMs: Long,
    durations: Durations,
    lights: UiLights
) {
    // Chuyển ms -> s (int, không âm)
    val timeLeftS = (timeLeftMs / 1000L).toInt().coerceAtLeast(0)

    // Thời gian "đỏ" tương đối (khi bên kia xanh+vàng)
    val redTimeA = ((durations.greenB_ms + durations.yellow_ms).toLong() / 1000L).toInt()
    val redTimeB = ((durations.greenA_ms + durations.yellow_ms).toLong() / 1000L).toInt()

    // Timer hiển thị cho từng hướng
    var aTimer = 0
    var bTimer = 0
    var aPhaseLabel = ""
    var bPhaseLabel = ""

    when (mode) {
        Mode.night -> {
            aPhaseLabel = "Nháy vàng"
            bPhaseLabel = "Nháy vàng"
        }
        Mode.emergency_A -> {
            aPhaseLabel = "Ưu tiên"
            bPhaseLabel = "Chờ"
        }
        Mode.emergency_B -> {
            aPhaseLabel = "Chờ"
            bPhaseLabel = "Ưu tiên"
        }
        Mode.emergency -> {
            aPhaseLabel = "Khẩn cấp"
            bPhaseLabel = "Khẩn cấp"
        }
        Mode.peak, Mode.default -> {
            val greenA_s = (durations.greenA_ms / 1000f)
            val yellow_ms = durations.yellow_ms
            val greenB_s = (durations.greenB_ms / 1000f)

            when (phase) {
                Phase.A_GREEN -> {
                    aPhaseLabel = "Đi"
                    aTimer = timeLeftS
                    bPhaseLabel = "Chờ"
                    // B đỏ còn lại = redTimeB - (đã trôi trong A_GREEN)
                    val elapsedA_s = greenA_s - timeLeftS
                    bTimer = (redTimeB - elapsedA_s.toInt()).coerceAtLeast(0)
                }
                Phase.A_YELLOW -> {
                    aPhaseLabel = "Đi chậm"
                    aTimer = timeLeftS
                    bPhaseLabel = "Chờ"
                    // Đã trôi trong A (xanh + vàng)
                    val elapsedA_s = (durations.greenA_ms + (yellow_ms - timeLeftMs)) / 1000
                    bTimer = (redTimeB - elapsedA_s.toInt()).coerceAtLeast(0)
                }
                Phase.ALL_RED_A2B -> {
                    aPhaseLabel = "Chờ"
                    bPhaseLabel = "Chờ"
                    // Pha dọn đường -> không cần đồng hồ
                }
                Phase.B_GREEN -> {
                    bPhaseLabel = "Đi"
                    bTimer = timeLeftS
                    aPhaseLabel = "Chờ"
                    val elapsedB_s = greenB_s - timeLeftS
                    aTimer = (redTimeA - elapsedB_s.toInt()).coerceAtLeast(0)
                }
                Phase.B_YELLOW -> {
                    bPhaseLabel = "Đi chậm"
                    bTimer = timeLeftS
                    aPhaseLabel = "Chờ"
                    val elapsedB_s = (durations.greenB_ms + (yellow_ms - timeLeftMs)) / 1000
                    aTimer = (redTimeA - elapsedB_s.toInt()).coerceAtLeast(0)
                }
                Phase.ALL_RED_B2A -> {
                    aPhaseLabel = "Chờ"
                    bPhaseLabel = "Chờ"
                }

                Phase.UNKNOWN -> TODO()
            }
        }
    }

    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                TrafficLightColumn(
                    isRedOn = lights.A_red,
                    isYellowOn = lights.A_yellow,
                    isGreenOn = lights.A_green,
                    label = "Hướng A",
                    timerSeconds = aTimer,
                    phaseLabel = aPhaseLabel
                )
                TrafficLightColumn(
                    isRedOn = lights.B_red,
                    isYellowOn = lights.B_yellow,
                    isGreenOn = lights.B_green,
                    label = "Hướng B",
                    timerSeconds = bTimer,
                    phaseLabel = bPhaseLabel
                )
            }
        }
    }
}

@Composable
fun TrafficLightColumn(
    isRedOn: Boolean,
    isYellowOn: Boolean,
    isGreenOn: Boolean,
    label: String,
    timerSeconds: Int,
    phaseLabel: String
) {
    Column(
        modifier = Modifier
            .background(Color(0xFF444444), RoundedCornerShape(8.dp))
            .padding(8.dp)
            .width(80.dp)
            .height(250.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ba đèn
        SignalLamp(on = isRedOn,    colorOn = Red,    size = 40.dp)
        SignalLamp(on = isYellowOn, colorOn = Yellow, size = 40.dp)
        SignalLamp(on = isGreenOn,  colorOn = Green, size = 40.dp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)

        if (phaseLabel.isNotEmpty()) {
            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hiển thị đồng hồ còn lại (nếu > 0)
        if (timerSeconds > 0) {
            CountdownRing(
                timeLeftSeconds = timerSeconds
            )
        }
    }
}

@Composable
fun SignalLamp(on: Boolean, colorOn: Color, size: Dp) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(size)
            .background(if (on) colorOn else colorOn.copy(alpha = 0.18f), CircleShape)
    )
}

@Composable
fun CountdownRing(
    timeLeftSeconds: Int,
    totalSeconds: Int = -1, // nếu có tổng thời gian riêng cho đèn này, truyền vào để show progress; nếu -1, chỉ hiển thị số
) {
    val progress = remember(timeLeftSeconds, totalSeconds) {
        if (totalSeconds > 0) timeLeftSeconds.toFloat() / totalSeconds.toFloat() else -1f
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(60.dp)
            .padding(4.dp)
    ) {
        if (progress >= 0f) {
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.size(60.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )
        }
        Text(
            text = timeLeftSeconds.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TrafficLight(color: Color, size: Dp) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(size)
            .background(color, CircleShape)
    )
}

@Composable
fun ModeStatusBadge(mode: String) {
    val (backgroundColor, textColor, label) = when (mode.lowercase()) {
        "default" -> Triple(Blue, Color.White, "Mặc định")
        "peak" -> Triple(Orange, Color.Black, "Cao điểm")
        "night" -> Triple(Color.DarkGray, Color.White, "Đêm")
        "emergency_a"-> Triple(Red, Color.White, "Khẩn cấp")
        "emergency_b"-> Triple(Red, Color.White, "Khẩn cấp")
        "emergency"-> Triple(Red, Color.White, "Khẩn cấp")
        else -> Triple(Color.Gray, Color.White, "Unknown")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight =  FontWeight.Bold
        )
    }
}

@Composable
fun ConnectionStatusBadge(
    label: String,
    online: Boolean?,        // đã “hiệu lực” từ repo.liveConnectionFlow
    agoMs: Long,             // serverNow - lastSeenAt (>=0)
    windowMs: Long = 20_000L // BEAT_MS
) {
    val cs = MaterialTheme.colorScheme

    val (container, content, dotTint, text) = when {
        online == true -> arrayOf(
            Green.copy(alpha = 0.2f), Color.Black , Green, "$label: Online"
        )
        agoMs < windowMs -> arrayOf(
            cs.secondaryContainer, cs.onSecondaryContainer, cs.secondary,
            "$label: Recovering…"
        )
        else -> arrayOf(
            Red.copy(alpha = 0.2f), Color.Black, Red,
            "$label: Offline"
        )
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = container as Color,
        contentColor = content as Color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // chấm trạng thái
            Box(
                Modifier
                    .size(10.dp)
                    .background(color = dotTint as Color, shape = CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(text as String, style = MaterialTheme.typography.labelLarge)
        }
    }
}

//fun timeAgo(lastSeenAt: Long): String {
//    if (lastSeenAt <= 0) return "n/a"
//    val diff = System.currentTimeMillis() - lastSeenAt
//    val sec = (diff / 1000).toInt()
//    val min = (sec / 60)
//    val hr  = (min / 60)
//    return when {
//        hr  > 0 -> "$hr h trước"
//        min > 0 -> "$min m trước"
//        else    -> "$sec s trước"
//    }
//}

@Composable
fun PhaseConfigCard(
    mode: Mode,
    durations: Durations,
    modifier: Modifier = Modifier
) {
    val aGreenS = (durations.greenA_ms / 1000).coerceAtLeast(0)
    val bGreenS = (durations.greenB_ms / 1000).coerceAtLeast(0)
    val yellowS = (durations.yellow_ms / 1000).coerceAtLeast(0)
    val clearS  = (durations.clear_ms / 1000).coerceAtLeast(0)

    // Red được suy ra theo chu kỳ:
    val aRedS = bGreenS + yellowS + clearS
    val bRedS = aGreenS + yellowS + clearS

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Bảng A/B
            Row(Modifier.fillMaxWidth()) {
                if (mode == Mode.night) {
                    PhaseCol1(
                        title = "Hướng A",
                        content = "Đi chậm và chú ý quan sát",
                        modifier = Modifier.weight(1f),
                        color = Yellow
                    )
                    Spacer(Modifier.width(48.dp))
                    PhaseCol1(
                        title = "Hướng B",
                        content = "Đi chậm và chú ý quan sát",
                        modifier = Modifier.weight(1f),
                        color = Yellow
                    )
                } else if (mode == Mode.emergency_A) {
                    PhaseCol1(
                        title = "Hướng A",
                        content = "Được ưu tiên",
                        modifier = Modifier.weight(1f),
                        color = Green
                    )
                    Spacer(Modifier.width(48.dp))
                    PhaseCol1(
                        title = "Hướng B",
                        content = "Dừng",
                        modifier = Modifier.weight(1f),
                        color = Red
                    )
                } else if (mode == Mode.emergency_B) {
                    PhaseCol1(
                        title = "Hướng A",
                        content = "Dừng",
                        modifier = Modifier.weight(1f),
                        color = Red
                    )
                    Spacer(Modifier.width(48.dp))
                    PhaseCol1(
                        title = "Hướng B",
                        content = "Được ưu tiên",
                        modifier = Modifier.weight(1f),
                        color = Green
                    )
                } else {
                    PhaseCol(
                        title = "Hướng A",
                        greenS = aGreenS,
                        yellowS = yellowS,
                        redS = aRedS,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(48.dp))
                    PhaseCol(
                        title = "Hướng B",
                        greenS = bGreenS,
                        yellowS = yellowS,
                        redS = bRedS,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PhaseCol(
    title: String,
    greenS: Int,
    yellowS: Int,
    redS: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            title,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Divider()
        Spacer(Modifier.height(8.dp))
        PhaseRow(label = "Xanh", value = "${greenS}s", color = Green)
        PhaseRow(label = "Vàng", value = "${yellowS}s", color = Yellow)
        PhaseRow(label = "Đỏ", value = "${redS}s", color = Red)
    }
}

@Composable
fun PhaseCol1(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Column(modifier) {
        Text(
            title,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Divider()
        Spacer(Modifier.height(8.dp))
        Text(
            content,
            textAlign = TextAlign.Center,
            color = color
        )
    }
}

@Composable
private fun PhaseRow(label: String, value: String, color: Color) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    icon: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text)
        }
    }
}

@Composable
fun FlashingYellowLight() {
    var isOn by remember { mutableStateOf(true) }
    val color by animateColorAsState(
        targetValue = if (isOn) Yellow else Color.Gray.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(500)
            isOn = !isOn
        }
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(40.dp)
            .background(color, CircleShape)
    )
}