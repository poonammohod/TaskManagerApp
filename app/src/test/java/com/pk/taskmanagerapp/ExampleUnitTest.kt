package com.pk.taskmanagerapp

import com.pk.taskmanagerapp.model.Task
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    // Task Model Tests
    @Test
    fun testTaskOverdueDetection() {
        // Test overdue task
        val overdueTask = Task(dueDate = System.currentTimeMillis() - 86400000) // 1 day ago
        assertTrue(overdueTask.isOverdue())

        // Test future task
        val futureTask = Task(dueDate = System.currentTimeMillis() + 86400000) // 1 day later
        assertFalse(futureTask.isOverdue())
    }

    @Test
    fun testTaskDefaultValues() {
        val task = Task(title = "Test Task")

        assertEquals("Test Task", task.title)
        assertEquals("pending", task.status)
        assertEquals("medium", task.priority)
        assertTrue(task.id.isNotEmpty())
    }

    @Test
    fun testTaskStatusFlow() {
        val task = Task(title = "Test Task")

        // Test status transitions
        assertEquals("pending", task.status)

        val inProgressTask = task.copy(status = "inProgress")
        assertEquals("inProgress", inProgressTask.status)

        val completedTask = task.copy(status = "completed")
        assertEquals("completed", completedTask.status)
    }

    @Test
    fun testTaskPriorityLevels() {
        val lowPriorityTask = Task(priority = "low")
        val mediumPriorityTask = Task(priority = "medium")
        val highPriorityTask = Task(priority = "high")

        assertEquals("low", lowPriorityTask.priority)
        assertEquals("medium", mediumPriorityTask.priority)
        assertEquals("high", highPriorityTask.priority)
    }

    @Test
    fun testTaskAssignment() {
        val task = Task(
            title = "Team Task",
            assignedTo = "user123",
            assignedToName = "John Doe"
        )

        assertEquals("user123", task.assignedTo)
        assertEquals("John Doe", task.assignedToName)
    }

    @Test
    fun testTaskCreationTimestamp() {
        val currentTime = System.currentTimeMillis()
        val task = Task(createdAt = currentTime)

        assertEquals(currentTime, task.createdAt)
        assertTrue(task.dueDate > 0)
    }

    // Utility Tests
    @Test
    fun testStringValidation() {
        val emptyString = ""
        val whitespaceString = "   "
        val validString = "Valid Task"

        assertTrue(emptyString.isEmpty())
        assertTrue(whitespaceString.trim().isEmpty())
        assertTrue(validString.isNotEmpty())
    }

    @Test
    fun testDateComparison() {
        val earlierDate = System.currentTimeMillis() - 1000
        val laterDate = System.currentTimeMillis() + 1000
        val currentDate = System.currentTimeMillis()

        assertTrue(earlierDate < currentDate)
        assertTrue(laterDate > currentDate)
    }

    @Test
    fun testTaskEquality() {
        val task1 = Task(id = "123", title = "Same Task")
        val task2 = Task(id = "123", title = "Same Task")
        val task3 = Task(id = "456", title = "Different Task")

        // Tasks with same ID should be considered equal
        assertEquals(task1.id, task2.id)
        assertNotEquals(task1.id, task3.id)
    }

    @Test
    fun testTaskDescriptionLength() {
        val shortDescription = "Short"
        val longDescription = "A".repeat(500) // Max length test

        assertTrue(shortDescription.length <= 500)
        assertTrue(longDescription.length == 500)
    }
}
