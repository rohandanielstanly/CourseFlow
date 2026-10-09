package com.courseflow.data.remote

import com.courseflow.data.local.SessionManager
import kotlinx.coroutines.delay

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    fun isLoggedIn(): Boolean
    fun logout()
}

/**
 * Mock login: any valid credentials succeed, except the password "wrong123" (to demo the error state).
 * On success the token is persisted, so the user stays logged in across launches (and offline).
 */
class MockAuthRepository(private val session: SessionManager) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<String> {
        delay(1200)
        if (password == "wrong123") {
            return Result.failure(IllegalArgumentException("Invalid email or password"))
        }
        val token = "mock-token-${email.hashCode()}"
        session.saveToken(token)
        return Result.success(token)
    }

    override fun isLoggedIn(): Boolean = session.isLoggedIn()

    override fun logout() = session.clear()
}
