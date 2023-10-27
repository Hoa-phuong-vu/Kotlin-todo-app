package com.example.customproject

import java.time.LocalTime

data class Todos(
    val taskId: String,
    var task: String,
    var dueTime: String,
    var isChecked: Boolean = false
)

