package com.example.customproject

import android.app.TimePickerDialog
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class PopUpFragment : DialogFragment() {
    private lateinit var tasktext: TextInputEditText
    private lateinit var timeButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private var isEditing = false
    private var taskToEdit: Todos? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pop_up, container, false)
        Log.d("PopUpFragment", "onCreate called")
        // Initialize your UI elements
        tasktext = view.findViewById(R.id.todoText)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("Tasks").child(auth.currentUser?.uid.toString())
        timeButton = view.findViewById(R.id.time)

        // Set a click listener for the time button
        timeButton.setOnClickListener {
            showTimePickerDialog()
        }

        val addbtn = view.findViewById<Button>(R.id.AddBtn)
        addbtn.setOnClickListener {
            val todoTask = tasktext.text.toString()
            val selectedTime = timeButton.text.toString()
            if (todoTask.isNotEmpty()) {
                val taskData = HashMap<String, String>()
                taskData["task"] = todoTask
                taskData["dueTime"] = selectedTime

                if (isEditing && taskToEdit != null) {
                    // If editing, update the existing task
                    databaseRef.child(taskToEdit!!.taskId).setValue(taskData).addOnCompleteListener { it ->
                        if (it.isSuccessful) {
                            Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show()
                            dismiss() // Dismiss the fragment after successfully editing the task
                        } else {
                            Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // If not editing, add a new task
                    databaseRef.push().setValue(taskData).addOnCompleteListener { it ->
                        if (it.isSuccessful) {
                            Toast.makeText(context, "Todo saved successfully", Toast.LENGTH_SHORT).show()
                            tasktext.text = null
                            timeButton.text = "Due Time"
                            dismiss() // Dismiss the fragment after successfully adding the task
                        } else {
                            Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Please type some todos", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }
    fun editTask(task: Todos) {
        isEditing = true
        taskToEdit = task
        Log.d("PopUpFragment", "editTask called")
        // Update UI elements with task data for editing
        val taskReference = databaseRef.child(task.taskId)
        taskReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val taskData = snapshot.value as Map<*, *>
                    val task = taskData["task"] as String
                    val dueTime = taskData["dueTime"] as String

                    tasktext.setText(task)
                    timeButton.text = dueTime
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors, e.g., task not found
                Toast.makeText(context, "Failed to fetch task details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showTimePickerDialog() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                // Format the selected time
                val selectedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                    }.time
                )

                // Set the selected time to the timeButton
                timeButton.text = selectedTime
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    fun initDatabaseRef(databaseRef: DatabaseReference) {
        this.databaseRef = databaseRef
    }


}
