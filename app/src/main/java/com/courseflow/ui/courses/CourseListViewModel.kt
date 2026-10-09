package com.courseflow.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courseflow.data.repo.CourseRepository
import com.courseflow.domain.Course
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CourseListUiState {
    data object Loading : CourseListUiState
    data class Success(val courses: List<Course>, val showingCachedData: Boolean) : CourseListUiState
    data object Empty : CourseListUiState
    data class Error(val message: String) : CourseListUiState
}

class CourseListViewModel(private val repository: CourseRepository) : ViewModel() {

    private sealed interface RefreshStatus {
        data object Loading : RefreshStatus
        data object Done : RefreshStatus
        data class Failed(val message: String) : RefreshStatus
    }

    private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.Loading)

    /**
     * UI state = what's in the local DB x the status of the last network refresh.
     * Cached courses always win over a failed refresh, which is the offline behaviour.
     */
    val state: StateFlow<CourseListUiState> = combine(
        repository.observeCourses(), refreshStatus
    ) { courses, status ->
        when {
            courses.isNotEmpty() -> CourseListUiState.Success(courses, showingCachedData = status is RefreshStatus.Failed)
            status is RefreshStatus.Loading -> CourseListUiState.Loading
            status is RefreshStatus.Failed -> CourseListUiState.Error(status.message)
            else -> CourseListUiState.Empty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseListUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshStatus.value = RefreshStatus.Loading
            refreshStatus.value = try {
                repository.refresh()
                RefreshStatus.Done
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RefreshStatus.Failed(e.message ?: "Unknown error")
            }
        }
    }
}
