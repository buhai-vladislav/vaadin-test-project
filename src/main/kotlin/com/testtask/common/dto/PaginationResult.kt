package com.testtask.common.dto

data class PaginationResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)
