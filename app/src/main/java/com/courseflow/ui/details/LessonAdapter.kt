package com.courseflow.ui.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.courseflow.R
import com.courseflow.databinding.ItemLessonBinding
import com.courseflow.domain.Lesson

class LessonAdapter(private val onMarkDone: (Lesson) -> Unit) :
    ListAdapter<Lesson, LessonAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemLessonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lesson = getItem(position)
        with(holder.binding) {
            lessonTitle.text = lesson.title
            lessonStatus.setText(if (lesson.isCompleted) R.string.completed else R.string.pending)
            lessonStatus.setTextColor(
                ContextCompat.getColor(
                    root.context,
                    if (lesson.isCompleted) R.color.cf_primary else R.color.cf_text_secondary
                )
            )
            markDoneButton.visibility = if (lesson.isCompleted) View.GONE else View.VISIBLE
            markDoneButton.setOnClickListener { onMarkDone(lesson) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(a: Lesson, b: Lesson) = a.id == b.id
        override fun areContentsTheSame(a: Lesson, b: Lesson) = a == b
    }
}
