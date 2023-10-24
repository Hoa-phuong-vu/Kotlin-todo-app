package com.example.customproject

import java.time.LocalTime

data class Todos(
    val taskId: String,
    val task: String,
//    val dueTime: LocalTime?
    var isChecked: Boolean = false
) {

}
