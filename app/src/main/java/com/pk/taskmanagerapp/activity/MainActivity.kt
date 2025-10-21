package com.pk.taskmanagerapp.activity

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pk.taskmanagerapp.R
import com.pk.taskmanagerapp.adapter.TaskAdapter
import com.pk.taskmanagerapp.model.Task
import com.pk.taskmanagerapp.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var repository: TaskRepository
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private lateinit var btnCreateTask: Button
    private lateinit var btnLogout: Button

    private var selectedDueDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_color)

        repository = TaskRepository()

        // Check if user is logged in
        if (!repository.isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initViews()
        setupUI()
        setupApp()
    }

    private fun initViews() {
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        btnCreateTask = findViewById(R.id.btnCreateTask)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupUI() {
        // Setup RecyclerView
        taskAdapter = TaskAdapter(
            emptyList(),
            onStatusChange = { taskId, newStatus ->
                updateTaskStatus(taskId, newStatus)
            },
            onDelete = { taskId ->
                deleteTask(taskId)
            }
        )

        recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }

        // Setup swipe to refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadTasks()
        }

        // Create task button
        btnCreateTask.setOnClickListener {
            showCreateTaskDialog()
        }

        // Logout button
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun setupApp() {
        val userId = repository.getCurrentUserId()
        val userName = repository.getCurrentUserName()

        if (userId != null) {
            loadTasks()
            setupRealtimeListeners()

            Toast.makeText(this, "Welcome, $userName!", Toast.LENGTH_SHORT).show()
            supportActionBar?.title = "My Tasks - $userName"
        }
    }

    private fun loadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = repository.getTasks()
                withContext(Dispatchers.Main) {
                    taskAdapter.updateTasks(tasks)
                    swipeRefreshLayout.isRefreshing = false

                    if (tasks.isEmpty()) {
                        Toast.makeText(this@MainActivity, "No tasks found. Create your first task!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun setupRealtimeListeners() {
        repository.listenForTaskUpdates { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            loadTasks()
        }
    }

    private fun showCreateTaskDialog() {
        val dialog = Dialog(this, R.style.CustomDialogTheme)
//        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_task)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val etTitle: EditText = dialog.findViewById(R.id.etTitle)
        val etDescription: EditText = dialog.findViewById(R.id.etDescription)
        val spinnerPriority: Spinner = dialog.findViewById(R.id.spinnerPriority)
        val btnSelectDate: Button = dialog.findViewById(R.id.btnSelectDate)
        val btnCreate: Button = dialog.findViewById(R.id.btnCreate)

        // Setup priority spinner
        val priorityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("Low", "Medium", "High")
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter

        // Set default due date to 7 days from now
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        selectedDueDate = calendar.timeInMillis
        btnSelectDate.text = "Due: ${android.text.format.DateFormat.getDateFormat(this).format(Date(selectedDueDate))}"

        // Date selection
        btnSelectDate.setOnClickListener {
            showDatePickerDialog { year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                selectedDueDate = calendar.timeInMillis
                btnSelectDate.text = "Due: ${android.text.format.DateFormat.getDateFormat(this).format(Date(selectedDueDate))}"
            }
        }

        // Create task
        btnCreate.setOnClickListener {
            val title = etTitle.text.toString()
            val description = etDescription.text.toString()
            val priority = spinnerPriority.selectedItem.toString()

            if (title.isBlank()) {
                Toast.makeText(this, "Please enter task title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val task = Task(
                title = title,
                description = description,
                assignedTo = repository.getCurrentUserId() ?: "user",
                assignedToName = repository.getCurrentUserName() ?: "You",
                priority = priority.toLowerCase(),
                dueDate = selectedDueDate
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.createTask(task)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Task created successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadTasks()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error creating task: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showDatePickerDialog(onDateSelected: (Int, Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                onDateSelected(year, month, day)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateTaskStatus(taskId: String, newStatus: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.updateTaskStatus(taskId, newStatus)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error updating status", Toast.LENGTH_SHORT).show()
                    loadTasks()
                }
            }
        }
    }

    private fun deleteTask(taskId: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                performDeleteTask(taskId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDeleteTask(taskId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.deleteTask(taskId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                    loadTasks()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error deleting task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logout() {
        repository.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }
}