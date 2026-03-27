package com.testtask.ui.dashboard

import com.testtask.domains.user.dto.FindUsersParams
import com.testtask.domains.user.dto.UserResponseDto
import com.testtask.domains.user.service.UserService
import com.testtask.security.SecurityUtils
import com.testtask.ui.common.pagination.PaginationController
import com.testtask.ui.dashboard.components.grid.UserGrid.build
import com.testtask.ui.dashboard.components.header.ToolbarHeader
import com.testtask.ui.dashboard.components.toolbar.Toolbar
import com.testtask.ui.dashboard.components.toolbar.dto.ToolbarParams
import com.testtask.ui.user.components.DeleteUserDialog
import com.testtask.ui.user.components.UserDialog
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.PermitAll

@Route("")
@PageTitle("Dashboard")
@PermitAll
class DashboardView(
    private val userService: UserService,
    private val authenticationContext: AuthenticationContext
) : VerticalLayout() {
    private val grid = Grid(UserResponseDto::class.java, false)
    private val toolbar = Toolbar()
    private val paginationController = PaginationController()

    private val isAdmin = SecurityUtils.isAdmin()

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true

        val toolbarHeader = ToolbarHeader.build(isAdmin) { authenticationContext.logout() }
        val toolbarComponent = toolbar.build(buildToolbarParams())
        val gridComponent = grid.build().addGridActions()
        val paginationBar = paginationController.buildPaginationBar(::refreshGrid)

        add(toolbarHeader, toolbarComponent, gridComponent, paginationBar)

        refreshGrid()
    }

    private fun refreshGrid() {
        val filter = toolbar.getUserFilters()
        val sort = toolbar.getUserSort()
        val (page, size) = paginationController.getPageAndSize()

        val params = FindUsersParams(filter, sort, page, size)
        val paginationResult = userService.findAllUsers(params)

        paginationController.applyPage(grid, paginationResult)
    }

    private fun Grid<UserResponseDto>.addGridActions(): Grid<UserResponseDto> {
        if (isAdmin) {
            this.addComponentColumn { user ->
                HorizontalLayout(
                    Button("Edit") { openEditDialog(user) },
                    Button("Delete") { openDeleteDialog(user) }
                )
            }.setHeader("Actions")
        }

        return this
    }

    private fun openCreateDialog() {
        UserDialog(
            userService = userService,
            userId = null,
            initialUser = null,
            onSuccess = ::onSuccessfulMutation
        ).open()
    }

    private fun openEditDialog(user: UserResponseDto) {
        UserDialog(
            userService = userService,
            userId = user.id,
            initialUser = user,
            onSuccess = ::refreshGrid
        ).open()
    }

    private fun openDeleteDialog(user: UserResponseDto) {
        DeleteUserDialog(
            userService = userService,
            userId = user.id,
            userName = user.name,
            onSuccess = ::onSuccessfulMutation
        ).open()
    }

    private fun onSuccessfulMutation() {
        paginationController.adjustPageAfterMutation()
        refreshGrid()
    }

    private fun buildToolbarParams() = ToolbarParams(
        isAdmin = isAdmin,
        pageSizeBox = paginationController.buildPageSizeBox(::refreshGrid),
        onFilterChange = ::refreshGrid,
        onUpsertUserClick = ::openCreateDialog,
        resetToFirstPage = { paginationController.resetToFirstPage() },
        resetPageSize = { paginationController.resetPageSize() }
    )
}