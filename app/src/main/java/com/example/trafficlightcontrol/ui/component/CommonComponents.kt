package com.example.trafficlightcontrol.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.trafficlightcontrol.ui.theme.Green

@Composable
fun LabelActive() {
    Surface(
        color = Green,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Text(
            "ĐANG HOẠT ĐỘNG",
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun LabelInactive() {
    Surface(
        color = Color.Gray,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Text(
            "KHÔNG HOẠT ĐỘNG",
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Số slider nguyên (dạng custom): cho phép chọn giá trị nguyên, hiển thị dãy số từ 10-150
 */
//@Composable
//fun NumberSlider(
//    value: Float,
//    onValueChange: (Float) -> Unit,
//    valueRange: ClosedFloatingPointRange<Float>,
//    step: Int
//) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Slider(
//            value = value,
//            onValueChange = onValueChange,
//            valueRange = valueRange,
//            steps = ((valueRange.endInclusive - valueRange.start) / step - 1).toInt(),
//            modifier = Modifier.padding(horizontal = 8.dp)
//        )
//        // Hiển thị dãy số chọn nhanh bên dưới (tuỳ chọn)
//        Row(
//            Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 8.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            for (num in listOf(10, 30, 60, 90, 120, 150)) {
//                TextButton(
//                    onClick = { onValueChange(num.toFloat()) },
//                    contentPadding = PaddingValues(0.dp)
//                ) {
//                    Text("$num", color = if (value.toInt() == num) Orange else Color.Gray)
//                }
//            }
//        }
//    }
//}

@Composable
fun ConfirmPopup(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        title = { Text(title) },
        text = { Text(message) },
        containerColor = MaterialTheme.colorScheme.surface
    )
}