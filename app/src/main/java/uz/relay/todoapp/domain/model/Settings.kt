package uz.relay.todoapp.domain.model

import java.time.LocalTime

data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultTime: LocalTime = LocalTime.of(9, 0),
    val alarmByDefault: Boolean = true,
    val vibration: Boolean = true,
    val onboarded: Boolean = false
)
