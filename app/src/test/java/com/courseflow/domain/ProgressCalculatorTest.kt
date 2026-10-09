package com.courseflow.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {

    @Test fun `percent matches the sample data`() {
        assertEquals(65, ProgressCalculator.percent(completed = 13, total = 20))
    }

    @Test fun `completing one more lesson increases progress and rounds to nearest`() {
        assertEquals(33, ProgressCalculator.percent(1, 3))
        assertEquals(67, ProgressCalculator.percent(2, 3))
        assertEquals(100, ProgressCalculator.percent(3, 3))
    }

    @Test fun `empty course never divides by zero`() {
        assertEquals(0, ProgressCalculator.percent(0, 0))
    }

    @Test fun `out-of-range input is clamped`() {
        assertEquals(100, ProgressCalculator.percent(99, 10))
        assertEquals(0, ProgressCalculator.percent(-5, 10))
    }

    @Test fun `server progress seeds the expected number of completed lessons`() {
        assertEquals(13, ProgressCalculator.completedCountFor(progress = 65, total = 20))
        assertEquals(7, ProgressCalculator.completedCountFor(progress = 25, total = 28))
        assertEquals(0, ProgressCalculator.completedCountFor(progress = 0, total = 16))
    }
}
