package uz.relay.todoapp.domain.model

enum class Priority(val title: String) {
    NONE("None"),
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    companion object {
        fun from(value: Int): Priority = entries.getOrElse(value) { NONE }
    }
}
