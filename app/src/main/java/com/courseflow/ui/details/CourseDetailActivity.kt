package com.courseflow.ui.details

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.courseflow.R
import com.courseflow.ServiceLocator
import com.courseflow.databinding.ActivityCourseDetailBinding
import com.courseflow.ui.ViewModelFactory
import kotlinx.coroutines.launch

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailBinding
    private val viewModel: CourseDetailViewModel by viewModels {
        val id = intent.getIntExtra(EXTRA_COURSE_ID, -1)
        ViewModelFactory { CourseDetailViewModel(id, ServiceLocator.courseRepository(applicationContext)) }
    }
    private val adapter = LessonAdapter { lesson -> viewModel.markCompleted(lesson.id) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.lessonList.layoutManager = LinearLayoutManager(this)
        binding.lessonList.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    state.course?.let { course ->
                        binding.toolbar.title = course.title
                        binding.progressBar.progress = course.progress
                        binding.progressText.text = getString(R.string.progress_fmt, course.progress)
                    }
                    adapter.submitList(state.lessons)
                }
            }
        }
    }

    companion object {
        const val EXTRA_COURSE_ID = "course_id"
    }
}
