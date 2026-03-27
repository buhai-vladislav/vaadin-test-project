package com.testtask.ui.dashboard.components.toolbar.dto

import com.vaadin.flow.component.combobox.ComboBox

data class ToolbarParams(
    val isAdmin: Boolean,
    val pageSizeBox: ComboBox<Int>,
    val onFilterChange: () -> Unit,
    val onUpsertUserClick: () -> Unit,
    val resetToFirstPage: () -> Unit,
    val resetPageSize: () -> Unit
)
