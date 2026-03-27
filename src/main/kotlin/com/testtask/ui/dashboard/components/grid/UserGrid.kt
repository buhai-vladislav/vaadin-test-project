package com.testtask.ui.dashboard.components.grid

import com.testtask.common.utils.DateFormatters.DATE_FORMAT
import com.testtask.domains.user.dto.UserResponseDto
import com.vaadin.flow.component.grid.Grid

object UserGrid {

    fun Grid<UserResponseDto>.build(): Grid<UserResponseDto> {
        this.setSizeFull()

        this.addColumn(UserResponseDto::name)
            .setHeader("Name")
            .isSortable = false

        this.addColumn(UserResponseDto::email)
            .setHeader("Email")
            .isSortable = false

        this.addColumn { it.createdAt.format(DATE_FORMAT) }
            .setHeader("Created At")
            .isSortable = false

        this.addColumn { it.updatedAt.format(DATE_FORMAT) }
            .setHeader("Updated At")
            .isSortable = false

        return this
    }
}