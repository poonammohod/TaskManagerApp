package com.pk.taskmanagerapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pk.taskmanagerapp.R
import com.pk.taskmanagerapp.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private var tasks: List<Task>,
    private val onStatusChange: (String, String) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onItemClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val tvAssignedTo: TextView = itemView.findViewById(R.id.tvAssignedTo)
        private val tvCurrentStatus: TextView = itemView.findViewById(R.id.tvCurrentStatus)
        private val priorityIndicator: View = itemView.findViewById(R.id.priorityIndicator)
        private val spinnerStatus: Spinner = itemView.findViewById(R.id.spinnerStatus)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(task: Task) {
            tvTitle.text = task.title
            tvDescription.text = task.description
            tvDueDate.text = "📅 ${dateFormat.format(Date(task.dueDate))}"
            tvPriority.text = task.priority.replaceFirstChar { it.uppercase() }
            tvAssignedTo.text = "👤 ${task.assignedToName}"

            // Show current status as text
            updateStatusText(task.status)

            // Set priority indicator color
            val priorityColor = when (task.priority) {
                "high" -> ContextCompat.getColor(itemView.context, R.color.priority_high)
                "medium" -> ContextCompat.getColor(itemView.context, R.color.priority_medium)
                else -> ContextCompat.getColor(itemView.context, R.color.priority_low)
            }
            priorityIndicator.setBackgroundColor(priorityColor)

            // Change card background if overdue
            if (task.isOverdue()) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.overdue_bg))
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.card_bg))
            }

            // Setup status spinner with visible text and emojis
            val statusOptions = arrayOf("⚪ Pending", "🟡 In Progress", "✅ Completed")
            val statusValues = arrayOf("pending", "inProgress", "completed")

            val statusAdapter = object : ArrayAdapter<String>(
                itemView.context,
                android.R.layout.simple_spinner_item,
                statusOptions
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.setTextColor(Color.BLACK)
                    textView.textSize = 14f
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.setTextColor(Color.BLACK)
                    textView.setBackgroundColor(Color.WHITE)
                    textView.textSize = 14f
                    textView.setPadding(16, 16, 16, 16)
                    return view
                }
            }

            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = statusAdapter

            // Set current status in spinner
            val currentPosition = when (task.status) {
                "pending" -> 0
                "inProgress" -> 1
                "completed" -> 2
                else -> 0
            }
            spinnerStatus.setSelection(currentPosition)

            // Status change listener
            spinnerStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val newStatus = statusValues[position]
                    if (newStatus != task.status) {
                        onStatusChange(task.id, newStatus)
                        // Update the status text immediately
                        updateStatusText(newStatus)
                    }
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

            // Delete button
            btnDelete.setOnClickListener {
                onDelete(task.id)
            }

            // Item click listener
            itemView.setOnClickListener {
                onItemClick?.invoke(task.id)
            }
        }

        private fun updateStatusText(status: String) {
            val statusText = when (status) {
                "pending" -> "⚪ Pending"
                "inProgress" -> "🟡 In Progress"
                "completed" -> "✅ Completed"
                else -> "⚪ Pending"
            }
            tvCurrentStatus.text = statusText

            val statusColor = when (status) {
                "completed" -> ContextCompat.getColor(itemView.context, R.color.status_completed)
                "inProgress" -> ContextCompat.getColor(itemView.context, R.color.status_inProgress)
                else -> ContextCompat.getColor(itemView.context, R.color.status_pending)
            }
            tvCurrentStatus.setTextColor(statusColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}

//package com.pk.taskmanagerapp.adapter
//
//import android.graphics.Color
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.Button
//import android.widget.Spinner
//import android.widget.TextView
//import androidx.cardview.widget.CardView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.pk.taskmanagerapp.R
//import com.pk.taskmanagerapp.model.Task
//import java.text.SimpleDateFormat
//import java.util.*
//
//class TaskAdapter(
//    private var tasks: List<Task>,
//    private val onStatusChange: (String, String) -> Unit,
//    private val onDelete: (String) -> Unit,
//    private val onItemClick: ((String) -> Unit)? = null
//) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
//
//    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//
//    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val cardView: CardView = itemView.findViewById(R.id.cardView)
//        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
//        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
//        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
//        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
//        private val tvAssignedTo: TextView = itemView.findViewById(R.id.tvAssignedTo)
//        private val tvCurrentStatus: TextView = itemView.findViewById(R.id.tvCurrentStatus)
//        private val priorityIndicator: View = itemView.findViewById(R.id.priorityIndicator)
//        private val spinnerStatus: Spinner = itemView.findViewById(R.id.spinnerStatus)
//        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
//
//        fun bind(task: Task) {
//            tvTitle.text = task.title
//            tvDescription.text = task.description
//            tvDueDate.text = "📅 ${dateFormat.format(Date(task.dueDate))}"
//            tvPriority.text = task.priority.replaceFirstChar { it.uppercase() }
//            tvAssignedTo.text = "👤 ${task.assignedToName}"
//
//            // Show current status as text
//            tvCurrentStatus.text = "Status: ${task.status.replaceFirstChar { it.uppercase() }}"
//
//            // Set status text color based on status
//            val statusColor = when (task.status) {
//                "completed" -> ContextCompat.getColor(itemView.context, R.color.status_completed)
//                "inProgress" -> ContextCompat.getColor(itemView.context, R.color.status_inProgress)
//                else -> ContextCompat.getColor(itemView.context, R.color.status_pending)
//            }
//            tvCurrentStatus.setTextColor(statusColor)
//
//            // Set priority indicator color
//            val priorityColor = when (task.priority) {
//                "high" -> ContextCompat.getColor(itemView.context, R.color.priority_high)
//                "medium" -> ContextCompat.getColor(itemView.context, R.color.priority_medium)
//                else -> ContextCompat.getColor(itemView.context, R.color.priority_low)
//            }
//            priorityIndicator.setBackgroundColor(priorityColor)
//
//            // Change card background if overdue
//            if (task.isOverdue()) {
//                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.overdue_bg))
//            } else {
//                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.card_bg))
//            }
//
//            // Setup status spinner
//            val statusAdapter = object : ArrayAdapter<String>(
//                itemView.context,
//                R.layout.spinner_item,
//                arrayOf("pending", "inProgress", "completed")
//            ) {
//                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
//                    val view = super.getDropDownView(position, convertView, parent)
//                    val textView = view.findViewById<TextView>(android.R.id.text1)
//                    textView.setTextColor(Color.BLACK)
//                    return view
//                }
//            }
//
//            statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
//            spinnerStatus.adapter = statusAdapter
//
//            // Set current status in spinner
//            val currentPosition = when (task.status) {
//                "pending" -> 0
//                "inProgress" -> 1
//                "completed" -> 2
//                else -> 0
//            }
//            spinnerStatus.setSelection(currentPosition)
//
//            // Status change listener
//            spinnerStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
//                    val newStatus = parent?.getItemAtPosition(position).toString()
//                    if (newStatus != task.status) {
//                        onStatusChange(task.id, newStatus)
//                        // Update the status text immediately
//                        tvCurrentStatus.text = "Status: ${newStatus.replaceFirstChar { it.uppercase() }}"
//                        val newStatusColor = when (newStatus) {
//                            "completed" -> ContextCompat.getColor(itemView.context, R.color.status_completed)
//                            "inProgress" -> ContextCompat.getColor(itemView.context, R.color.status_inProgress)
//                            else -> ContextCompat.getColor(itemView.context, R.color.status_pending)
//                        }
//                        tvCurrentStatus.setTextColor(newStatusColor)
//                    }
//                }
//                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
//            }
//
//            // Delete button
//            btnDelete.setOnClickListener {
//                onDelete(task.id)
//            }
//
//            // Item click listener
//            itemView.setOnClickListener {
//                onItemClick?.invoke(task.id)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_task, parent, false)
//        return TaskViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
//        holder.bind(tasks[position])
//    }
//
//    override fun getItemCount() = tasks.size
//
//    fun updateTasks(newTasks: List<Task>) {
//        tasks = newTasks
//        notifyDataSetChanged()
//    }
//}
