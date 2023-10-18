package com.example.customproject

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
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
        registerEvents()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
           return true
        }
        return super.onOptionsItemSelected(item)
    }
    //write the tasks & add them
    private fun registerEvents(){
        val expand = findViewById<ImageView>(R.id.add)
        val addTodoLayout = findViewById<LinearLayout>(R.id.addTodoLayout)

        expand.setOnClickListener{
            // Toggle the visibility of the addTodoLayout
            if (addTodoLayout.visibility == View.VISIBLE) {
                addTodoLayout.visibility = View.GONE // Hide the layout
            } else {
                addTodoLayout.visibility = View.VISIBLE // Show the layout
            }

        }

        val addbtn = findViewById<ImageView>(R.id.AddBtn)
        val tasktext = findViewById<TextInputEditText>(R.id.todoText)

        addbtn.setOnClickListener{
            val todoTask = tasktext.text.toString()
            if(todoTask.isNotEmpty()){
                databaseRef.push().setValue(todoTask).addOnCompleteListener{
                    if(it.isSuccessful){
                        Toast.makeText(this, "Todo saved successfully", Toast.LENGTH_SHORT).show()
                        tasktext.text = null
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
    private fun getDataFromFirebase(){
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                newList.clear()
                for (taskSnapshot in snapshot.children) {
                    val todoTask =
                        taskSnapshot.key?.let { Todos(it, taskSnapshot.value.toString()) }

                    if (todoTask != null) {
                        newList.add(todoTask)
                    }

                }
                adapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
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

    }


}