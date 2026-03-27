package com.testtask.domains.user.dto

data class FindUsersParams(
    val filter: UserFilter,
    val sort: UserSort,
    val page: Int,
    val size: Int
)
