package com.example.trafficlightcontrol.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    fullScreen: Boolean = false
) {
    if (fullScreen) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    } else {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier.size(36.dp)
        )
    }
}