package com.courseflow.ui.courses

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.courseflow.R
import com.courseflow.ServiceLocator
import com.courseflow.databinding.ActivityCourseListBinding
import com.courseflow.ui.ViewModelFactory
import com.courseflow.ui.details.CourseDetailActivity
import com.courseflow.ui.login.LoginActivity
import kotlinx.coroutines.launch

class CourseListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseListBinding
    private val viewModel: CourseListViewModel by viewModels {
        ViewModelFactory { CourseListViewModel(ServiceLocator.courseRepository(applicationContext)) }
    }
    private val adapter = CourseAdapter { course ->
        startActivity(
            Intent(this, CourseDetailActivity::class.java)
                .putExtra(CourseDetailActivity.EXTRA_COURSE_ID, course.id)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.inflateMenu(R.menu.menu_courses)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                logout()
                true
            } else false
        }

        binding.courseList.layoutManager = LinearLayoutManager(this)
        binding.courseList.adapter = adapter
        binding.retryButton.setOnClickListener { viewModel.refresh() }
        binding.offlineBanner.setOnClickListener { viewModel.refresh() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.state.collect(::render) }
        }
    }

    private fun logout() {
        ServiceLocator.authRepository(applicationContext).logout()
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun render(state: CourseListUiState) = with(binding) {
        loading.visibility = if (state is CourseListUiState.Loading) View.VISIBLE else View.GONE
        courseList.visibility = if (state is CourseListUiState.Success) View.VISIBLE else View.GONE
        emptyText.visibility = if (state is CourseListUiState.Empty) View.VISIBLE else View.GONE
        errorGroup.visibility = if (state is CourseListUiState.Error) View.VISIBLE else View.GONE
        offlineBanner.visibility =
            if (state is CourseListUiState.Success && state.showingCachedData) View.VISIBLE else View.GONE

        when (state) {
            is CourseListUiState.Success -> adapter.submitList(state.courses)
            is CourseListUiState.Error -> errorText.text = getString(R.string.load_error_fmt, state.message)
            else -> Unit
        }
    }
}
