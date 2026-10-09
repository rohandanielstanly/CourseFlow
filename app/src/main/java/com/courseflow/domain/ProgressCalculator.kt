package com.courseflow.domain

import kotlin.math.roundToInt

/** Pure business logic for course progress. No Android dependencies, so it is trivially unit-testable. */
object ProgressCalculator {

    /** Completion percentage (0..100), rounded to the nearest integer. */
    fun percent(completed: Int, total: Int): Int {
        if (total <= 0) return 0
        return ((completed.coerceIn(0, total) * 100.0) / total).roundToInt()
    }

    /** How many lessons should be marked done to approximate a server-reported progress %. */
    fun completedCountFor(progress: Int, total: Int): Int =
        ((progress.coerceIn(0, 100) * total) / 100.0).roundToInt().coerceIn(0, maxOf(total, 0))
}
