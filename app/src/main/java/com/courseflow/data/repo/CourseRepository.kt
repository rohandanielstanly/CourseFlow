package com.courseflow.data.repo

import androidx.room.withTransaction
import com.courseflow.data.local.AppDatabase
import com.courseflow.data.local.CourseEntity
import com.courseflow.data.local.LessonEntity
import com.courseflow.data.local.toDomain
import com.courseflow.data.remote.CourseRemoteDataSource
import com.courseflow.domain.Course
import com.courseflow.domain.Lesson
import com.courseflow.domain.ProgressCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth = Room. The UI only ever observes the database; [refresh] syncs
 * remote -> local. That is what makes offline "just work".
 */
class CourseRepository(
    private val db: AppDatabase,
    private val remote: CourseRemoteDataSource
) {
    private val courseDao = db.courseDao()
    private val lessonDao = db.lessonDao()

    fun observeCourses(): Flow<List<Course>> = courseDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeCourse(id: Int): Flow<Course?> = courseDao.observe(id).map { it?.toDomain() }

    fun observeLessons(courseId: Int): Flow<List<Lesson>> =
        lessonDao.observe(courseId).map { list -> list.map { it.toDomain() } }

    /** Fetches remote data and merges it into the cache. Throws (e.g. IOException) on failure. */
    suspend fun refresh() {
        val courses = remote.fetchCourses()
        val existing = courseDao.getIds().toSet()

        // Network calls happen before the DB transaction so it stays short.
        val newLessons = courses.filter { it.id !in existing }.associate { course ->
            course.id to remote.fetchLessonTitles(course.id)
                .ifEmpty { List(course.lessons) { i -> "Lesson ${i + 1}" } }
        }

        db.withTransaction {
            courses.forEach { dto ->
                if (dto.id in existing) {
                    // Keep local progress: the user's completions must not be overwritten by a refresh.
                    courseDao.updateMeta(dto.id, dto.title, dto.instructor, dto.lessons)
                } else {
                    courseDao.insert(CourseEntity(dto.id, dto.title, dto.instructor, dto.progress, dto.lessons))
                    val titles = newLessons.getValue(dto.id)
                    val done = ProgressCalculator.completedCountFor(dto.progress, titles.size)
                    lessonDao.insertAll(titles.mapIndexed { i, title ->
                        LessonEntity(courseId = dto.id, position = i, title = title, completed = i < done)
                    })
                }
            }
        }
    }

    /** Marks the lesson done and recomputes course progress atomically. Works offline. */
    suspend fun completeLesson(lessonId: Long, courseId: Int) {
        db.withTransaction {
            lessonDao.markCompleted(lessonId)
            val percent = ProgressCalculator.percent(lessonDao.completedCount(courseId), lessonDao.count(courseId))
            courseDao.updateProgress(courseId, percent)
        }
    }
}
