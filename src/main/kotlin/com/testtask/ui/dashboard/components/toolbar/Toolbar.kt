package com.testtask.ui.dashboard.components.toolbar

import com.testtask.domains.user.dto.UserFilter
import com.testtask.domains.user.dto.UserSort
import com.testtask.domains.user.enums.UserSortField
import com.testtask.ui.dashboard.components.toolbar.dto.ToolbarParams
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.TextField
import org.springframework.data.domain.Sort

class Toolbar {
    private val nameFilter = TextField("Filter by name")
    private val emailFilter = TextField("Filter by email")

    private val sortField = ComboBox<UserSortField>("Sort by")
    private val sortDirection = ComboBox<Sort.Direction>("Direction")

    fun build(params: ToolbarParams): HorizontalLayout {
        val (isAdmin, pageSizeBox, _, onUpsertUserClick, _) = params
        val nameFilter = FilterItem.build("Filter by name", this.nameFilter) {
            onFilterChange(params)
        }

        val emailFilter = FilterItem.build("Filter by email", this.emailFilter) {
            onFilterChange(params)
        }

        return HorizontalLayout(
            nameFilter,
            emailFilter,
            buildSortField(params),
            buildSortDirection(params),
            pageSizeBox,
            buildClearButton(params),
            buildCreateUserButton(isAdmin, onUpsertUserClick)
        ).apply {
            width = "100%"
            defaultVerticalComponentAlignment = FlexComponent.Alignment.END
        }
    }

    fun getUserFilters() = UserFilter(
        name = nameFilter.value,
        email = emailFilter.value
    )

    fun getUserSort() = UserSort(
        field = sortField.value ?: UserSortField.NAME,
        direction = sortDirection.value ?: Sort.Direction.ASC
    )

    private fun onFilterChange(params: ToolbarParams) {
        params.resetToFirstPage()
        params.onFilterChange()
    }

    private fun buildSortField(params: ToolbarParams): ComboBox<UserSortField> {
        sortField.setItems(*UserSortField.entries.toTypedArray())
        sortField.value = UserSortField.NAME
        sortField.addValueChangeListener {
            params.resetToFirstPage()
            params.onFilterChange()
        }

        return sortField
    }

    private fun buildSortDirection(params: ToolbarParams): ComboBox<Sort.Direction> {
        sortDirection.setItems(*Sort.Direction.entries.toTypedArray())
        sortDirection.value = Sort.Direction.ASC
        sortDirection.addValueChangeListener {
            params.resetToFirstPage()
            params.onFilterChange()
        }

        return sortDirection
    }

    private fun buildClearButton(params: ToolbarParams) = Button("Clear filters") {
        nameFilter.clear()
        emailFilter.clear()

        sortField.value = UserSortField.NAME
        sortDirection.value = Sort.Direction.ASC

        params.resetPageSize()
        params.resetToFirstPage()
        params.onFilterChange()
    }

    private fun buildCreateUserButton(isAdmin: Boolean, onClick: () -> Unit) = Button("Create user").apply {
        isEnabled = isAdmin
        isVisible = isAdmin
        addClickListener { onClick() }
    }
}