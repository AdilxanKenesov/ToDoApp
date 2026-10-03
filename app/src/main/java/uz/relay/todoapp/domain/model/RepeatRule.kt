package uz.relay.todoapp.domain.model

enum class RepeatRule(val title: String) {
    NONE("Once"),
    DAILY("Daily"),
    WEEKDAYS("Weekdays"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    companion object {
        fun from(value: String?): RepeatRule = entries.firstOrNull { it.name == value } ?: NONE
    }
}
