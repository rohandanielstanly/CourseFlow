package com.courseflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY id")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    fun observe(id: Int): Flow<CourseEntity?>

    @Query("SELECT id FROM courses")
    suspend fun getIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity)

    /** Refreshes server-owned fields only; local progress is preserved. */
    @Query("UPDATE courses SET title = :title, instructor = :instructor, lessons = :lessons WHERE id = :id")
    suspend fun updateMeta(id: Int, title: String, instructor: String, lessons: Int)

    @Query("UPDATE courses SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: Int, progress: Int)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY position")
    fun observe(courseId: Int): Flow<List<LessonEntity>>

    @Insert
    suspend fun insertAll(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET completed = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId AND completed = 1")
    suspend fun completedCount(courseId: Int): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    suspend fun count(courseId: Int): Int
}
