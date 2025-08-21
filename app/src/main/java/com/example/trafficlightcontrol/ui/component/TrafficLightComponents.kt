package com.example.trafficlightcontrol.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ModeCard(
    title: String,
    color: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .padding(8.dp)
            .clickable( onClick = onClick ),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TrafficLightDisplay(
    mode: String,
    currentPhase: String,
    timeLeft: Int,
    modifier: Modifier = Modifier
) {
    val isPhaseA = currentPhase == "A"

    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Trạng thái hiện tại",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                ModeStatusBadge(mode = mode)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
//                // Đèn giao thông hướng A
//                TrafficLightColumn(
//                    isRedOn = !isPhaseA,
//                    isYellowOn = false,
//                    isGreenOn = isPhaseA,
//                    label = "Hướng A"
//                )
//
//                // Đồng hồ đếm ngược
//                CountdownTimer(timeLeft = timeLeft)
//
//                // Đèn giao thông hướng B
//                TrafficLightColumn(
//                    isRedOn = isPhaseA,
//                    isYellowOn = false,
//                    isGreenOn = !isPhaseA,
//                    label = "Hướng B"
//                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Đèn giao thông hướng A
                    TrafficLightColumn(
                        isRedOn = !isPhaseA,
                        isYellowOn = false,
                        isGreenOn = isPhaseA,
                        label = "Hướng A"
                    )

                    // Đồng hồ đếm ngược
                    CountdownTimer(timeLeft = timeLeft)
                }

                Column (
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Đèn giao thông hướng B
                    TrafficLightColumn(
                        isRedOn = isPhaseA,
                        isYellowOn = false,
                        isGreenOn = !isPhaseA,
                        label = "Hướng B"
                    )

                    // Đồng hồ đếm ngược
                    CountdownTimer(timeLeft = timeLeft)
                }
            }
        }
    }
}

@Composable
fun TrafficLightColumn(
    isRedOn: Boolean,
    isYellowOn: Boolean,
    isGreenOn: Boolean,
    label: String
) {
    Column(
        modifier = Modifier
            .background(Color(0xFF444444), RoundedCornerShape(8.dp))
            .padding(8.dp)
            .width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Đèn đỏ
        TrafficLight(
            color = if (isRedOn) Red else Color.Gray.copy(alpha = 0.3f),
            size = 40.dp
        )

        // Đèn vàng
        TrafficLight(
            color = if (isYellowOn) Yellow else Color.Gray.copy(alpha = 0.3f),
            size = 40.dp
        )

        // Đèn xanh
        TrafficLight(
            color = if (isGreenOn) Green else Color.Gray.copy(alpha = 0.3f),
            size = 40.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TrafficLight(color: Color, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(size)
            .background(color, CircleShape)
    )
}

@Composable
fun CountdownTimer(timeLeft: Int) {
    var currentTime by remember { mutableIntStateOf(timeLeft) }
    val animatedProgress = remember { Animatable(1f) }

    LaunchedEffect(timeLeft) {
        currentTime = timeLeft
        animatedProgress.snapTo(1f)
        animatedProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = timeLeft * 1000,
                easing = LinearEasing
            )
        )
    }

    // Đếm ngược
    LaunchedEffect(timeLeft) {
        currentTime = timeLeft
        while (currentTime > 0) {
            delay(1000)
            currentTime--
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .padding(8.dp)
    ) {
        // Vòng tròn tiến độ
        CircularProgressIndicator(
            progress = animatedProgress.value,
            modifier = Modifier.size(80.dp),
            color = MaterialTheme.colorScheme.onBackground,
            strokeWidth = 4.dp
        )

        // Hiển thị số giây còn lại
        Text(
            text = currentTime.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModeStatusBadge(mode: String) {
    val (backgroundColor, textColor, label) = when (mode.lowercase()) {
        "scheduled" -> Triple(Blue, Color.White, "Theo lịch")
        "peak" -> Triple(Orange, Color.Black, "Cao điểm")
        "night" -> Triple(Color.DarkGray, Color.White, "Đêm")
        "emergency" -> Triple(Red, Color.White, "Khẩn cấp")
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
fun ConnectionStatusIndicator(isConnected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        val color = if (isConnected) Green else Red
        val text = if (isConnected) "Kết nối" else "Mất kết nối"

        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun FormattedDateTime(timestamp: Long) {
    val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
    val date = Date(timestamp)
    Text(text = dateFormat.format(date), style = MaterialTheme.typography.labelSmall)
}

@Composable
fun FormattedDate(timestamp: Long) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = Date(timestamp)
    Text(text = dateFormat.format(date), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp))
}

@Composable
fun FormattedTime(timestamp: Long) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val date = Date(timestamp)
    Text(
        text = dateFormat.format(date),
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
    )
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