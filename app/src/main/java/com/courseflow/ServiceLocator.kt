package com.courseflow

import android.content.Context
import com.courseflow.data.local.AppDatabase
import com.courseflow.data.local.SessionManager
import com.courseflow.data.remote.AuthRepository
import com.courseflow.data.remote.MockAuthRepository
import com.courseflow.data.remote.MockCourseRemoteDataSource
import com.courseflow.data.repo.CourseRepository

/** Tiny manual DI. In a larger app this would be Hilt/Koin. */
object ServiceLocator {
    @Volatile private var courseRepository: CourseRepository? = null
    @Volatile private var authRepository: AuthRepository? = null

    fun authRepository(context: Context): AuthRepository {
        authRepository?.let { return it }
        return synchronized(this) {
            authRepository ?: MockAuthRepository(SessionManager(context.applicationContext))
                .also { authRepository = it }
        }
    }

    fun courseRepository(context: Context): CourseRepository {
        courseRepository?.let { return it }
        return synchronized(this) {
            courseRepository ?: run {
                val app = context.applicationContext
                CourseRepository(AppDatabase.get(app), MockCourseRemoteDataSource(app))
            }.also { courseRepository = it }
        }
    }
}
