package com.example.customproject

import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Paint
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customproject.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), TodoAdapter.TaskAdapterInterface {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var newRecyclerView: RecyclerView
    private lateinit var adapter: TodoAdapter
    private lateinit var newList: MutableList<Todos>
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("Tasks").child(auth.currentUser?.uid.toString())

        // Set up the navigation drawer
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navIcon = binding.navIcon
        navIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set up the navigation menu
        binding.menu.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    startActivity(Intent(this@MainActivity, LogInActivity::class.java))
                    true
                }
                else -> false
            }
        }

//         Initialize the RecyclerView
        newRecyclerView = findViewById(R.id.recyclerView)
        newRecyclerView.setHasFixedSize(true)
        newRecyclerView.layoutManager = LinearLayoutManager(this)

//        set adapter to the RecyclerView
        newList = mutableListOf()
        adapter = TodoAdapter(newList)
        adapter.setListener(this)
        newRecyclerView.adapter = adapter

        getDataFromFirebase()
//        registerEvents()
        val expand = findViewById<ImageView>(R.id.add)

        expand.setOnClickListener{
            val fragment = PopUpFragment()
            fragment.show(supportFragmentManager, "newTask")

        }


    }
    fun addNewTask(newTask: Todos) {
        // Add the new task to your data source
        newList.add(newTask)

        // Notify the RecyclerView adapter that the data has changed
        adapter.notifyItemInserted(newList.size - 1)
    }


    //write the tasks & add them
    private fun registerEvents(){

        val addTodoLayout = findViewById<LinearLayout>(R.id.addTodoLayout)
        val addbtn = findViewById<Button>(R.id.AddBtn)
        val tasktext = findViewById<TextInputEditText>(R.id.todoText)
        val timeBtn = findViewById<Button>(R.id.time)
        val dueTimeTextView = findViewById<TextView>(R.id.dueTime)


        timeBtn.setOnClickListener {
            val currentTime = java.util.Calendar.getInstance() // Use java.util.Calendar
            val hour = currentTime.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(java.util.Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val selectedTime =  String.format("%02d:%02d", selectedHour, selectedMinute)
                    timeBtn.text = selectedTime
                },
                hour,
                minute,
                true
            )
            timePickerDialog.show()
        }


        //add todos
        addbtn.setOnClickListener{
                val todoTask = tasktext.text.toString()
                val selectedTime = timeBtn.text.toString()
                if (todoTask.isNotEmpty()) {
                    val taskData = HashMap<String, String>()
                    taskData["task"] = todoTask
                    taskData["dueTime"] = selectedTime
                    databaseRef.push().setValue(taskData).addOnCompleteListener{
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Todo saved successfully", Toast.LENGTH_SHORT)
                                .show()
                            tasktext.text = null
                            timeBtn.text = "Due Time"
                            dueTimeTextView.text = "Due Time: $selectedTime"
                        } else {
                            Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                        addTodoLayout.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this, "Please type some todos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    //get the tasks from database
    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                newList.clear()
                for (taskSnapshot in snapshot.children) {
                    val taskId = taskSnapshot.key
                    val task = taskSnapshot.child("task").getValue(String::class.java)
                    val dueTime = taskSnapshot.child("dueTime").getValue(String::class.java)

                    if (taskId != null && task != null && dueTime != null) {
                        val todoTask = Todos(taskId, task, dueTime)
                        newList.add(todoTask)
                        Log.d("FirebaseData", "Task ID: $taskId, Task: $task, Due Time: $dueTime")
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Firebase database error: ${error.message}")
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }



    //delete tasks
    override fun onDeleteItemClicked(todos: Todos, position: Int) {
        databaseRef.child(todos.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //edit tasks
    override fun onEditItemClicked(todos: Todos, position: Int) {
        val taskReference = databaseRef.child(todos.taskId)
        taskReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val taskData = snapshot.value as Map<*, *>
                    val task = taskData["task"] as String
                    val dueTime = taskData["dueTime"] as String

                    // Update the existing 'todos' object with fetched data
                    todos.task = task
                    todos.dueTime = dueTime

                    val fragment = PopUpFragment()
                    fragment.show(supportFragmentManager, "editTask")
                    fragment.initDatabaseRef(databaseRef)// Show the fragment
                    fragment.editTask(todos) // Set the task data for editing
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors, e.g., task not found
                Toast.makeText(applicationContext, "Failed to fetch task details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    //toggles the nav drawer
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onUncheckClicked(todos: Todos, position: Int) {
        todos.isChecked = !todos.isChecked
        adapter.notifyItemChanged(position)

    }

}