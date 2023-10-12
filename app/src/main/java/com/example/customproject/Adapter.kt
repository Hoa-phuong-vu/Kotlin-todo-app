package com.example.customproject

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
                editTask.setOnClickListener {
                    listener?.onEditItemClicked(this , position)
                }

                deleteTask.setOnClickListener {
                    listener?.onDeleteItemClicked(this , position)
                }
            }
        }

    }

    interface TaskAdapterInterface{
        fun onDeleteItemClicked(todos: Todos, position : Int)
        fun onEditItemClicked(todos: Todos, position: Int)
    }

}