package com.courseflow.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class CourseDto(val id: Int, val title: String, val instructor: String, val progress: Int, val lessons: Int)

interface CourseRemoteDataSource {
    @Throws(IOException::class)
    suspend fun fetchCourses(): List<CourseDto>

    @Throws(IOException::class)
    suspend fun fetchLessonTitles(courseId: Int): List<String>
}

/**
 * Mock API backed by bundled JSON. It adds latency and throws [IOException] when the device is
 * offline, so turning on airplane mode behaves like a real network failure.
 * Swap for a Retrofit implementation without touching the repository.
 */
class MockCourseRemoteDataSource(private val context: Context) : CourseRemoteDataSource {

    override suspend fun fetchCourses(): List<CourseDto> = withContext(Dispatchers.IO) {
        simulateNetwork(800)
        val array = JSONArray(readAsset("courses.json"))
        List(array.length()) { i ->
            val o = array.getJSONObject(i)
            CourseDto(o.getInt("id"), o.getString("title"), o.getString("instructor"), o.getInt("progress"), o.getInt("lessons"))
        }
    }

    override suspend fun fetchLessonTitles(courseId: Int): List<String> = withContext(Dispatchers.IO) {
        simulateNetwork(150)
        val array = JSONObject(readAsset("lessons.json")).optJSONArray(courseId.toString())
            ?: return@withContext emptyList()
        List(array.length()) { array.getString(it) }
    }

    private suspend fun simulateNetwork(latencyMs: Long) {
        delay(latencyMs)
        if (!isOnline()) throw IOException("No internet connection")
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun readAsset(name: String) = context.assets.open(name).bufferedReader().use { it.readText() }
}
