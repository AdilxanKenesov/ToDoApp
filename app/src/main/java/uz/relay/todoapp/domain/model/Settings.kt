package uz.relay.todoapp.domain.model

data class Settings(
    val alarmByDefault: Boolean = true,
    val vibration: Boolean = true,
    val onboarded: Boolean = false
)
