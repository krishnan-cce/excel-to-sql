package com.udyata.excelbook

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExcelViewModel : ViewModel() {
    private val _columns = MutableStateFlow<List<String>>(emptyList())
    val columns: StateFlow<List<String>> = _columns.asStateFlow()

    private val _columnMappings = MutableStateFlow<Map<String, String>>(emptyMap())
    val columnMappings: StateFlow<Map<String, String>> = _columnMappings.asStateFlow()

    fun setColumns(columns: List<String>) {
        _columns.value = columns
    }

    fun setColumnMappings(mappings: Map<String, String>) {
        _columnMappings.value = mappings
    }

    fun updateColumnMapping(column: String, type: String) {
        _columnMappings.value = _columnMappings.value.toMutableMap().apply {
            put(column, type)
        }
    }
}