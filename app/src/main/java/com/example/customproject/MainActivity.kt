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
import androidx.recyclerview.widget.ItemTouchHelper
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
                R.id.favPage -> {
                    // Filter out favorite tasks
                    val expand = findViewById<ImageView>(R.id.add)
                    expand.visibility = View.GONE
                        val favoriteTasks = newList.filter { it.isFavorite }
                        newList.clear()
                        newList.addAll(favoriteTasks)
                        adapter.notifyDataSetChanged()
                    true
                }
                R.id.home -> {
                    //return to the originial list
                    val expand = findViewById<ImageView>(R.id.add)
                    expand.visibility = View.VISIBLE
                    getDataFromFirebase()
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

        val swipeToDelete = object :SwipeToDelete(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(direction) {
                    ItemTouchHelper.LEFT -> {
                        val position = viewHolder.bindingAdapterPosition
                        val deletedItem = newList[position]

                        onDeleteItem(deletedItem, position)
                    }
                }
            }
        }
        val touchHelper = ItemTouchHelper(swipeToDelete)
        touchHelper.attachToRecyclerView(newRecyclerView)

        adapter.setListener(this)
        newRecyclerView.adapter = adapter

        getDataFromFirebase()
        val expand = findViewById<ImageView>(R.id.add)

        expand.setOnClickListener{
            val fragment = PopUpFragment()
            fragment.show(supportFragmentManager, "newTask")

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
                    val isChecked = taskSnapshot.child("isChecked").getValue(Boolean::class.java) ?: false
                    val isFavorite = taskSnapshot.child("isFavorite").getValue(Boolean::class.java) ?: false


                    if (taskId != null && task != null && dueTime != null) {
                        val todoTask = Todos(taskId, task, dueTime,isChecked, isFavorite)
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
    override fun onDeleteItem(todos: Todos, position: Int) {
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
        updateStatusInDatabase(todos, "isChecked", todos.isChecked)

    }

    override fun onFavoriteClicked(todos: Todos, position: Int) {
        todos.isFavorite = !todos.isFavorite
        // Update the RecyclerView to reflect the changes
        adapter.notifyItemChanged(position)
        // Update the database with the new favorite status
        updateStatusInDatabase(todos, "isFavorite", todos.isFavorite)
    }

    private fun updateStatusInDatabase(todos: Todos, key: String, value: Any) {
        val taskReference = databaseRef.child(todos.taskId)
        val updates = mapOf(key to value)

        taskReference.updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    when (key) {
                        "isFavorite" -> {
                            if (todos.isFavorite) {
                                Toast.makeText(this, "Marked as favorite", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                            }
                        }
                        "isChecked" -> {
                            if (todos.isChecked) {
                                Toast.makeText(this, "Marked as checked", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Marked as unchecked", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Handle the case where the database update failed
                    Toast.makeText(this, "Failed to update $key status", Toast.LENGTH_SHORT).show()
                }
            }
    }



}


