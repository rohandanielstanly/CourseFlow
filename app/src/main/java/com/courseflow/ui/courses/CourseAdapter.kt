package com.courseflow.ui.courses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.courseflow.R
import com.courseflow.databinding.ItemCourseBinding
import com.courseflow.domain.Course

class CourseAdapter(private val onCourseClick: (Course) -> Unit) :
    ListAdapter<Course, CourseAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = getItem(position)
        with(holder.binding) {
            val ctx = root.context
            titleText.text = course.title
            instructorText.text = ctx.getString(R.string.instructor_fmt, course.instructor)
            lessonsText.text = ctx.getString(R.string.lessons_fmt, course.lessonCount)
            progressBar.progress = course.progress
            progressText.text = ctx.getString(R.string.progress_fmt, course.progress)
            continueButton.setOnClickListener { onCourseClick(course) }
            root.setOnClickListener { onCourseClick(course) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(a: Course, b: Course) = a.id == b.id
        override fun areContentsTheSame(a: Course, b: Course) = a == b
    }
}
