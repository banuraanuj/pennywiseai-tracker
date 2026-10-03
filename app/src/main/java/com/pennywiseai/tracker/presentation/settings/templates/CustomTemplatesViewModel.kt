package com.pennywiseai.tracker.presentation.settings.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pennywiseai.tracker.data.database.dao.CustomTemplateDao
import com.pennywiseai.tracker.data.database.entity.CustomTemplateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomTemplatesViewModel @Inject constructor(
    private val dao: CustomTemplateDao
) : ViewModel() {

    private val _templates = MutableStateFlow<List<CustomTemplateEntity>>(emptyList())
    val templates: StateFlow<List<CustomTemplateEntity>> = _templates.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAllTemplates().collect {
                _templates.value = it
            }
        }
    }
}