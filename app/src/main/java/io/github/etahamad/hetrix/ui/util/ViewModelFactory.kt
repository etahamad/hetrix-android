package io.github.etahamad.hetrix.ui.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.etahamad.hetrix.data.repository.MonitorRepository
import io.github.etahamad.hetrix.ui.main.MonitorsViewModel

/**
 * ViewModelProvider.Factory for instantiating ViewModels with repository dependencies.
 */
class HetrixViewModelFactory(
    private val repository: MonitorRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonitorsViewModel::class.java)) {
            return MonitorsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
