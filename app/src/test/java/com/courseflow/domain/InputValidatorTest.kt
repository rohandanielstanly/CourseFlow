package com.courseflow.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {
    @Test fun `accepts normal emails and rejects malformed ones`() {
        assertTrue(InputValidator.isValidEmail("jane.doe+test@example.co.in"))
        assertFalse(InputValidator.isValidEmail("jane@"))
        assertFalse(InputValidator.isValidEmail("jane.example.com"))
        assertFalse(InputValidator.isValidEmail(""))
    }

    @Test fun `password needs at least six characters`() {
        assertFalse(InputValidator.isValidPassword("12345"))
        assertTrue(InputValidator.isValidPassword("123456"))
    }
}
