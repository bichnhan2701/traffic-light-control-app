package com.example.trafficlightcontrol.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.ui.theme.*

@Composable
fun TrafficLightDisplay(
    direction: Direction,
    currentPhase: LightPhase,
    remainingTime: Int,
    isManualOverride: Boolean,
    modifier: Modifier = Modifier
) {
    val redAlpha by animateFloatAsState(
        targetValue = if (currentPhase == LightPhase.RED) 1f else 0.3f,
        label = "redAlpha"
    )
    val yellowAlpha by animateFloatAsState(
        targetValue = if (currentPhase == LightPhase.YELLOW) 1f else 0.3f,
        label = "yellowAlpha"
    )
    val greenAlpha by animateFloatAsState(
        targetValue = if (currentPhase == LightPhase.GREEN) 1f else 0.3f,
        label = "greenAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = direction.name.replace("_", " "),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Traffic Light Container
        Box(
            modifier = Modifier
                .size(120.dp, 300.dp)
                .padding(8.dp)
        ) {
            // Background
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(Color.DarkGray)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.matchParentSize()
            ) {
                // Red Light
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(80.dp)
                ) {
                    Canvas(modifier = Modifier
                        .matchParentSize()
                        .alpha(redAlpha)
                    ) {
                        val radius = size.minDimension / 2
                        drawCircle(
                            color = Red,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = radius
                        )
                    }
                    if (currentPhase == LightPhase.RED) {
                        Text(
                            text = "$remainingTime",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Yellow Light
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(80.dp)
                ) {
                    Canvas(modifier = Modifier
                        .matchParentSize()
                        .alpha(yellowAlpha)
                    ) {
                        val radius = size.minDimension / 2
                        drawCircle(
                            color = Yellow,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = radius
                        )
                    }
                    if (currentPhase == LightPhase.YELLOW) {
                        Text(
                            text = "$remainingTime",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Green Light
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(80.dp)
                ) {
                    Canvas(modifier = Modifier
                        .matchParentSize()
                        .alpha(greenAlpha)
                    ) {
                        val radius = size.minDimension / 2
                        drawCircle(
                            color = Green,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = radius
                        )
                    }
                    if (currentPhase == LightPhase.GREEN) {
                        Text(
                            text = "$remainingTime",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (isManualOverride) {
            Text(
                text = "MANUAL OVERRIDE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}