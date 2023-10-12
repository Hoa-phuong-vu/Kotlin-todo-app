package com.example.customproject

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("Tasks").child(auth.currentUser?.uid.toString())

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

    override fun onDeleteItemClicked(todos: Todos, position: Int) {
        databaseRef.child(todos.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditItemClicked(todos: Todos, position: Int) {

    }

    private fun updateTask(todos: Todos, todoEdit: TextInputEditText) {
        val map = HashMap<String, Any>()
        map[todos.taskId] = todos.task
        databaseRef.updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Updated Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }

}