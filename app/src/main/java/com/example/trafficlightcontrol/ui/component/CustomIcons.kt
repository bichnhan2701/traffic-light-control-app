package com.example.trafficlightcontrol.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

object CustomIcons {
    val TrafficSignal: ImageVector
        get() {
            if (Icons.Default::class.java.declaredFields.any { it.name == "TrafficSignal" }) {
                return Icons.Default::class.java.getDeclaredField("TrafficSignal")
                    .get(Icons.Default) as ImageVector
            }
            return materialIcon("TrafficSignal") {
                materialPath {
                    moveTo(12.0f, 2.0f)
                    horizontalLineTo(8.0f)
                    curveTo(6.9f, 2.0f, 6.0f, 2.9f, 6.0f, 4.0f)
                    verticalLineTo(20.0f)
                    curveTo(6.0f, 21.1f, 6.9f, 22.0f, 8.0f, 22.0f)
                    horizontalLineTo(16.0f)
                    curveTo(17.1f, 22.0f, 18.0f, 21.1f, 18.0f, 20.0f)
                    verticalLineTo(4.0f)
                    curveTo(18.0f, 2.9f, 17.1f, 2.0f, 16.0f, 2.0f)
                    horizontalLineTo(12.0f)
                    close()
                    moveTo(12.0f, 11.0f)
                    curveTo(10.9f, 11.0f, 10.0f, 10.1f, 10.0f, 9.0f)
                    curveTo(10.0f, 7.9f, 10.9f, 7.0f, 12.0f, 7.0f)
                    curveTo(13.1f, 7.0f, 14.0f, 7.9f, 14.0f, 9.0f)
                    curveTo(14.0f, 10.1f, 13.1f, 11.0f, 12.0f, 11.0f)
                    close()
                    moveTo(12.0f, 17.0f)
                    curveTo(10.9f, 17.0f, 10.0f, 16.1f, 10.0f, 15.0f)
                    curveTo(10.0f, 13.9f, 10.9f, 13.0f, 12.0f, 13.0f)
                    curveTo(13.1f, 13.0f, 14.0f, 13.9f, 14.0f, 15.0f)
                    curveTo(14.0f, 16.1f, 13.1f, 17.0f, 12.0f, 17.0f)
                    close()
                    moveTo(12.0f, 23.0f)
                    curveTo(10.9f, 23.0f, 10.0f, 22.1f, 10.0f, 21.0f)
                    curveTo(10.0f, 19.9f, 10.9f, 19.0f, 12.0f, 19.0f)
                    curveTo(13.1f, 19.0f, 14.0f, 19.9f, 14.0f, 21.0f)
                    curveTo(14.0f, 22.1f, 13.1f, 23.0f, 12.0f, 23.0f)
                    close()
                }
            }
        }
}