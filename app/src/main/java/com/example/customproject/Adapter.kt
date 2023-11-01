package com.example.customproject

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter (private val list:MutableList<Todos>) :
RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    private  val TAG = "TaskAdapter"
    private var listener:TaskAdapterInterface? = null

    fun setListener(listener: TaskAdapterInterface) {
        this.listener = listener
    }


    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoTask: TextView = itemView.findViewById(R.id.todoTask)
        val editTask: ImageView = itemView.findViewById(R.id.editTask)
        val favouriteTask: ImageView = itemView.findViewById(R.id.favourite)
        val completeButton: ImageView = itemView.findViewById(R.id.checkButton)
        val dueTimeTextView: TextView = itemView.findViewById(R.id.dueTime)
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
                dueTimeTextView.text = this.dueTime

                // Change the favorite icon based on the isFavorite property
                if (this.isFavorite) {
                    favouriteTask.setImageResource(R.drawable.star)
                } else {
                    favouriteTask.setImageResource(R.drawable.starborder)
                }

                //toggle checkbox outline
                if (this.isChecked) {
                    completeButton.setImageResource(R.drawable.check)
                    completeButton.setColorFilter(Color.MAGENTA)
                    todoTask.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    dueTimeTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                } else {
                    completeButton.setImageResource(R.drawable.uncheck)
                    completeButton.setColorFilter(Color.BLACK)
                    todoTask.paintFlags = 0
                    dueTimeTextView.paintFlags = 0
                }


                Log.d(TAG, "onBindViewHolder: $this")
                //favourite icon
                favouriteTask.setOnClickListener {
                    listener?.onFavoriteClicked(this, position)
                }
                // Set click listener for the edit button
                editTask.setOnClickListener {
                    listener?.onEditItemClicked(this , position)
                }
                // Checkbox
                completeButton.setOnClickListener {
                    listener?.onUncheckClicked(this, position)
                }
            }
        }
    }


    interface TaskAdapterInterface{
        fun onDeleteItem(todos: Todos, position : Int)
        fun onEditItemClicked(todos: Todos, position: Int)
        fun onFavoriteClicked(todos: Todos, position: Int)
        fun onUncheckClicked(todos: Todos, position: Int)

    }





}