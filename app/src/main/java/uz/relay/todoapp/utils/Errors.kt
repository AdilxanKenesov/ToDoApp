package uz.relay.todoapp.utils

/** Short, human message for an error shown in a snackbar. */
fun Throwable.userMessage(): String = message?.takeIf { it.isNotBlank() } ?: "Something went wrong"
