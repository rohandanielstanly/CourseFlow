package com.courseflow.domain

object InputValidator {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    const val MIN_PASSWORD_LENGTH = 6

    fun isValidEmail(email: String) = EMAIL.matches(email.trim())
    fun isValidPassword(password: String) = password.length >= MIN_PASSWORD_LENGTH
}
