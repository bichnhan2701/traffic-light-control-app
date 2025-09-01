package com.example.trafficlightcontrol.data.helper

import com.example.trafficlightcontrol.data.model.*

/* =========================
 *  Logic mô phỏng tại client
 * ========================= */

fun phaseTotalMs(phase: Phase, mode: Mode, d: Durations): Long = when {
    mode == Mode.night -> 0L // client tự nháy vàng; pha không chạy
    else -> when (phase) {
        Phase.A_GREEN     -> d.greenA_ms.toLong()
        Phase.A_YELLOW    -> d.yellow_ms.toLong()
        Phase.ALL_RED_A2B -> d.clear_ms.toLong()
        Phase.B_GREEN     -> d.greenB_ms.toLong()
        Phase.B_YELLOW    -> d.yellow_ms.toLong()
        Phase.ALL_RED_B2A -> d.clear_ms.toLong()
        Phase.UNKNOWN -> TODO()
    }
}

fun deriveLights(mode: Mode, phase: Phase, serverNow: Long): UiLights {
    if (mode == Mode.night) {
        // client tự nháy vàng ~2Hz
        val blink = ((serverNow / 500) % 2L) == 0L
        return UiLights(A_yellow = blink, B_yellow = blink)
    }
    if (mode == Mode.emergency_A) return UiLights(A_green = true, B_red = true)
    if (mode == Mode.emergency_B) return UiLights(A_red = true, B_green = true)

    return when (phase) {
        Phase.A_GREEN     -> UiLights(A_green = true, B_red = true)
        Phase.A_YELLOW    -> UiLights(A_yellow = true, B_red = true)
        Phase.ALL_RED_A2B -> UiLights(A_red = true, B_red = true)
        Phase.B_GREEN     -> UiLights(A_red = true, B_green = true)
        Phase.B_YELLOW    -> UiLights(A_red = true, B_yellow = true)
        Phase.ALL_RED_B2A -> UiLights(A_red = true, B_red = true)
        Phase.UNKNOWN -> TODO()
    }
}