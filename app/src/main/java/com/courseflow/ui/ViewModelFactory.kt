package com.courseflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <V : ViewModel> create(modelClass: Class<V>): V = creator() as V
}
