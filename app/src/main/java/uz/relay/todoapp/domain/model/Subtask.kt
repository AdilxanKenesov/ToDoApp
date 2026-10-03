package uz.relay.todoapp.domain.model

data class Subtask(
    val id: Long = 0,
    val title: String,
    val done: Boolean = false
)
