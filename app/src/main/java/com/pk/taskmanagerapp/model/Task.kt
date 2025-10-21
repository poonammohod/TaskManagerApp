package com.pk.taskmanagerapp.model

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val assignedTo: String = "",
    val assignedToName: String = "",
    val createdBy: String = "",
    val status: String = "pending",
    val priority: String = "medium",
    val createdAt: Long = 0L,
    val dueDate: Long = 0L
) {
    fun isOverdue(): Boolean {
        return dueDate < System.currentTimeMillis() && status != "completed"
    }
}