package com.example.customproject

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class TodoAdapter (private val list:MutableList<Todos>) :
RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    private  val TAG = "TaskAdapter"
    private var isEditing = false
    private var editedPosition = -1
    private var listener:TaskAdapterInterface? = null
    fun setListener(listener: TaskAdapterInterface) {
        this.listener = listener
    }


    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoTask: TextView = itemView.findViewById(R.id.todoTask)
        val editTask: ImageView = itemView.findViewById(R.id.editTask)
        val deleteTask: ImageView = itemView.findViewById(R.id.deleteTask)
        val completeButton: ImageView = itemView.findViewById(R.id.checkButton)
        val dueTime: TextView = itemView.findViewById(R.id.dueTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.activity_todos,
            parent,
            false
        )
        return TodoViewHolder(itemView)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {

        with(holder) {
            with(list[position]) {
                todoTask.text = this.task


                Log.d(TAG, "onBindViewHolder: $this")
                // Set click listener for the edit button
                editTask.setOnClickListener {
                    listener?.onEditItemClicked(this , position)
                }
                // Set click listener for the delete button
                deleteTask.setOnClickListener {
                    listener?.onDeleteItemClicked(this , position)
                }
                completeButton.setOnClickListener {
                    listener?.onUncheckClicked(this, position)
                }
            }
        }

    }

    interface TaskAdapterInterface{
        fun onDeleteItemClicked(todos: Todos, position : Int)
        fun onEditItemClicked(todos: Todos, position: Int)
        fun onUncheckClicked(todos: Todos, position: Int)
    }

}