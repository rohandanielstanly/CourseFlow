package com.courseflow.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courseflow.data.repo.CourseRepository
import com.courseflow.domain.Course
import com.courseflow.domain.Lesson
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val course: Course? = null,
    val lessons: List<Lesson> = emptyList()
)

class CourseDetailViewModel(
    private val courseId: Int,
    private val repository: CourseRepository
) : ViewModel() {

    val state: StateFlow<CourseDetailUiState> = combine(
        repository.observeCourse(courseId),
        repository.observeLessons(courseId)
    ) { course, lessons -> CourseDetailUiState(course, lessons) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseDetailUiState())

    /** The DB write re-emits both flows, so lesson status and course progress update automatically. */
    fun markCompleted(lessonId: Long) {
        viewModelScope.launch { repository.completeLesson(lessonId, courseId) }
    }
}
