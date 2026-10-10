package com.belectronicsvp.ui.dashboardlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belectronicsvp.data.PanelRepository
import com.belectronicsvp.model.PanelModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardListViewModel(
    private val repository: PanelRepository
) : ViewModel() {

    val panels: StateFlow<List<PanelModel>> = repository.panelsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.getAllPanels()
        )

    fun createPanel(
        name: String,
        description: String = "",
        orientation: com.belectronicsvp.model.PanelOrientation = com.belectronicsvp.model.PanelOrientation.LANDSCAPE,
        baseDensity: Int = 8
    ): PanelModel {
        return repository.createPanel(name, description, orientation, baseDensity)
    }

    fun renamePanel(id: String, newName: String) {
        repository.renamePanel(id, newName)
    }

    fun clonePanel(id: String, newName: String = "") {
        repository.clonePanel(id, newName)
    }

    fun deletePanel(id: String) {
        repository.deletePanel(id)
    }

    fun getExportJson(id: String): String? {
        return repository.exportToJson(id).getOrNull()
    }

    fun importPanel(json: String): Result<PanelModel> {
        return repository.importFromJson(json)
    }
}
