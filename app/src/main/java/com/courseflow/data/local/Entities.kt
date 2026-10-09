package com.courseflow.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.courseflow.domain.Course
import com.courseflow.domain.Lesson

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessons: Int
)

@Entity(
    tableName = "lessons",
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("courseId")]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Int,
    val position: Int,
    val title: String,
    val completed: Boolean
)

fun CourseEntity.toDomain() = Course(id, title, instructor, progress, lessons)
fun LessonEntity.toDomain() = Lesson(id, courseId, title, completed)
