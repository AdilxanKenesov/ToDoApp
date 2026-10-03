package uz.relay.todoapp.domain.model

data class TaskList(
    val id: Long = 0,
    val name: String,
    val color: Int = 0,
    val icon: ListIcon = ListIcon.LIST,
    val taskCount: Int = 0,
    val doneCount: Int = 0
) {
    val progress: Float get() = if (taskCount == 0) 0f else doneCount / taskCount.toFloat()
}

enum class ListIcon {
    LIST, WORK, HOME, CART, TRAVEL, STUDY, HEALTH, STAR, MONEY, GIFT;

    companion object {
        fun from(value: String?): ListIcon = entries.firstOrNull { it.name == value } ?: LIST
    }
}
