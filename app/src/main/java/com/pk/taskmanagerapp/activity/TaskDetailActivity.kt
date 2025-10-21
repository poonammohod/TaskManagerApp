package com.pk.taskmanagerapp.activity

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.pk.taskmanagerapp.R
import com.pk.taskmanagerapp.model.Task
import com.pk.taskmanagerapp.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var repository: TaskRepository
    private var currentTask: Task? = null

    // Views
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvAssignedTo: TextView
    private lateinit var tvCreatedBy: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var spinnerStatus: Spinner
    private lateinit var spinnerPriority: Spinner
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        repository = TaskRepository()
        initViews()
        setupClickListeners()

        val taskId = intent.getStringExtra("TASK_ID")
        if (taskId != null) {
            loadTaskDetails(taskId)
        } else {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        tvAssignedTo = findViewById(R.id.tvAssignedTo)
        tvCreatedBy = findViewById(R.id.tvCreatedBy)
        tvDueDate = findViewById(R.id.tvDueDate)
        tvCreatedAt = findViewById(R.id.tvCreatedAt)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnBack = findViewById(R.id.btnBack)

        // Setup spinners
        setupStatusSpinner()
        setupPrioritySpinner()
    }

    private fun setupStatusSpinner() {
        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("pending", "inProgress", "completed")
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter
    }

    private fun setupPrioritySpinner() {
        val priorityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("low", "medium", "high")
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter
    }

    private fun setupClickListeners() {
        btnUpdate.setOnClickListener {
            updateTask()
        }

        btnDelete.setOnClickListener {
            deleteTask()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadTaskDetails(taskId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = repository.getTasks()
                val task = tasks.find { it.id == taskId }

                withContext(Dispatchers.Main) {
                    if (task != null) {
                        currentTask = task
                        displayTaskDetails(task)
                    } else {
                        Toast.makeText(this@TaskDetailActivity, "Task not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "Error loading task details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayTaskDetails(task: Task) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

        tvTitle.text = task.title
        tvDescription.text = task.description
        tvAssignedTo.text = "Assigned to: ${task.assignedToName}"
        tvCreatedBy.text = "Created by: ${task.createdBy}"
        tvDueDate.text = "Due: ${dateFormat.format(Date(task.dueDate))}"
        tvCreatedAt.text = "Created: ${dateFormat.format(Date(task.createdAt))}"

        // Set spinners
        spinnerStatus.setSelection(when (task.status) {
            "pending" -> 0
            "inProgress" -> 1
            "completed" -> 2
            else -> 0
        })

        spinnerPriority.setSelection(when (task.priority) {
            "low" -> 0
            "medium" -> 1
            "high" -> 2
            else -> 1
        })

        // Change background if overdue
        if (task.isOverdue()) {
            findViewById<LinearLayout>(R.id.layoutContainer).setBackgroundColor(
                resources.getColor(R.color.light_red)
            )
        }
    }

    private fun updateTask() {
        val task = currentTask ?: return

        val newStatus = spinnerStatus.selectedItem.toString()
        val newPriority = spinnerPriority.selectedItem.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.updateTaskStatus(task.id, newStatus)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "Task updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "Error updating task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteTask() {
        val task = currentTask ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.deleteTask(task.id)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "Error deleting task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}