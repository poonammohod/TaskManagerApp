package com.pk.taskmanagerapp.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.pk.taskmanagerapp.model.Task
import com.pk.taskmanagerapp.model.User
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance()

    // Auth Operations
    suspend fun registerUser(email: String, password: String, name: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            // Update user profile with name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            result.user?.updateProfile(profileUpdates)?.await()

            // Create user in Firestore
            createUserInFirestore(result.user?.uid ?: "", name, email)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName ?: auth.currentUser?.email?.split("@")?.get(0)
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Firestore Operations
    private suspend fun createUserInFirestore(userId: String, name: String, email: String) {
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId).set(user).await()
    }

    suspend fun getCurrentUser(): User? {
        val userId = auth.currentUser?.uid ?: return null
        val document = db.collection("users").document(userId).get().await()
        val data = document.data
        return User(
            id = document.id,
            name = data?.get("name") as? String ?: auth.currentUser?.displayName ?: "Unknown User",
            email = data?.get("email") as? String ?: auth.currentUser?.email ?: "",
            createdAt = (data?.get("createdAt") as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0
        )
    }

    suspend fun getAllUsers(): List<User> {
        val documents = db.collection("users").get().await()
        return documents.documents.map { doc ->
            val data = doc.data
            User(
                id = doc.id,
                name = data?.get("name") as? String ?: "Unknown User",
                email = data?.get("email") as? String ?: "",
                createdAt = (data?.get("createdAt") as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0
            )
        }
    }

    suspend fun createTask(task: Task): String {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        val currentUserName = getCurrentUserName() ?: "Unknown User"

        val taskData = hashMapOf(
            "title" to task.title,
            "description" to task.description,
            "assignedTo" to task.assignedTo,
            "assignedToName" to task.assignedToName,
            "createdBy" to currentUserId,
            "createdByName" to currentUserName,
            "status" to task.status,
            "priority" to task.priority,
            "createdAt" to FieldValue.serverTimestamp(),
            "dueDate" to task.dueDate
        )

        val result = db.collection("tasks").add(taskData).await()

        // Update Realtime Database for real-time notifications
        updateTaskInRealtimeDB(result.id)
        return result.id
    }

    suspend fun getTasks(): List<Task> {
        val documents = db.collection("tasks")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()

        return documents.documents.map { doc ->
            val data = doc.data
            Task(
                id = doc.id,
                title = data?.get("title") as? String ?: "",
                description = data?.get("description") as? String ?: "",
                assignedTo = data?.get("assignedTo") as? String ?: "",
                assignedToName = data?.get("assignedToName") as? String ?: "",
                createdBy = data?.get("createdBy") as? String ?: "",
                status = data?.get("status") as? String ?: "pending",
                priority = data?.get("priority") as? String ?: "medium",
                createdAt = (data?.get("createdAt") as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0,
                dueDate = data?.get("dueDate") as? Long ?: 0
            )
        }
    }

    suspend fun updateTaskStatus(taskId: String, status: String) {
        db.collection("tasks").document(taskId).update(
            "status", status
        ).await()
        updateTaskInRealtimeDB(taskId)
    }

    suspend fun deleteTask(taskId: String) {
        db.collection("tasks").document(taskId).delete().await()
    }

    // Realtime Database Operations
    private fun updateTaskInRealtimeDB(taskId: String) {
        val taskUpdateRef = realtimeDb.getReference("task_updates").child(taskId)

        val update = hashMapOf<String, Any>(
            "lastUpdate" to ServerValue.TIMESTAMP,
            "updatedBy" to (auth.currentUser?.uid ?: "unknown"),
            "updatedByName" to (getCurrentUserName() ?: "unknown")
        )

        taskUpdateRef.setValue(update)
    }

    fun setupUserPresence(userId: String) {
        val presenceRef = realtimeDb.getReference("presence").child(userId)

        val onlineStatus = hashMapOf<String, Any>(
            "status" to "online",
            "lastSeen" to ServerValue.TIMESTAMP,
            "userName" to (getCurrentUserName() ?: "Unknown User")
        )

        presenceRef.setValue(onlineStatus)

        val offlineStatus = hashMapOf<String, Any>(
            "status" to "offline",
            "lastSeen" to ServerValue.TIMESTAMP,
            "userName" to (getCurrentUserName() ?: "Unknown User")
        )

        presenceRef.onDisconnect().setValue(offlineStatus)
    }

    fun listenForTaskUpdates(callback: (String) -> Unit) {
        val updatesRef = realtimeDb.getReference("task_updates")
        updatesRef.addChildEventListener(object : com.google.firebase.database.ChildEventListener {
            override fun onChildAdded(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                val updatedBy = snapshot.child("updatedByName").getValue(String::class.java) ?: "Someone"
                callback("$updatedBy updated a task")
            }
            override fun onChildRemoved(snapshot: com.google.firebase.database.DataSnapshot) {}
            override fun onChildMoved(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }
}