package com.courseflow.domain

data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessonCount: Int
)

data class Lesson(
    val id: Long,
    val courseId: Int,
    val title: String,
    val isCompleted: Boolean
)
