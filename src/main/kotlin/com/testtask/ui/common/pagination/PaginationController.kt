package com.testtask.ui.common.pagination

import com.testtask.common.dto.PaginationResult
import com.testtask.domains.user.dto.UserResponseDto
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout

class PaginationController {
    private val pageSizeBox = ComboBox<Int>("Page size")
    private val pageInfo = Span()
    private val totalInfo = Span()
    private val previousButton = Button("Previous")
    private val nextButton = Button("Next")

    private var currentPage = INIT_PAGE
    private var currentPageSize = DEFAULT_PAGE_SIZE
    private var currentTotalPages = 0

    fun buildPageSizeBox(refreshGrid: () -> Unit): ComboBox<Int> {
        pageSizeBox.setItems(*PAGE_SIZE_OPTIONS)
        pageSizeBox.value = currentPageSize
        pageSizeBox.addValueChangeListener {
            currentPageSize = it.value ?: DEFAULT_PAGE_SIZE
            resetToFirstPage()
            refreshGrid()
        }

        return pageSizeBox
    }

    fun buildPaginationBar(refreshGrid: () -> Unit): HorizontalLayout {
        previousButton.addClickListener {
            if (currentPage > 0) {
                currentPage--
                refreshGrid()
            }
        }

        nextButton.addClickListener {
            if (currentPage + 1 < currentTotalPages) {
                currentPage++
                refreshGrid()
            }
        }

        return HorizontalLayout(previousButton, nextButton, pageInfo, totalInfo).apply {
            width = "100%"
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }
    }

    fun resetPageSize() {
        pageSizeBox.value = DEFAULT_PAGE_SIZE
        currentPageSize = DEFAULT_PAGE_SIZE
        currentPage = INIT_PAGE
    }

    fun resetToFirstPage() {
        currentPage = 0
    }

    fun adjustPageAfterMutation() {
        if (currentPage > 0 && currentPage + 1 >= currentTotalPages) {
            currentPage--
        }
    }

    fun applyPage(grid: Grid<UserResponseDto>, paginationResult: PaginationResult<UserResponseDto>) {
        grid.setItems(paginationResult.items)

        currentPage = paginationResult.page
        currentPageSize = paginationResult.size
        currentTotalPages = paginationResult.totalPages

        val currentPageHuman = if (paginationResult.totalPages == 0) 0 else paginationResult.page + 1

        pageInfo.text = "Page $currentPageHuman of ${paginationResult.totalPages}"
        totalInfo.text = "Total users: ${paginationResult.totalItems}"

        previousButton.isEnabled = paginationResult.page > 0
        nextButton.isEnabled = paginationResult.page + 1 < paginationResult.totalPages
    }

    fun getPageAndSize() = Pair(currentPage, currentPageSize)

    companion object {
        private val PAGE_SIZE_OPTIONS = arrayOf(5, 10, 20, 50, 100)
        private const val DEFAULT_PAGE_SIZE = 5
        private const val INIT_PAGE = 0
    }
}