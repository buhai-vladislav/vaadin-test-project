package com.testtask.domains.user.dto

import com.testtask.domains.user.enums.UserSortField
import org.springframework.data.domain.Sort

data class UserSort(
    val field: UserSortField = UserSortField.NAME,
    val direction: Sort.Direction = Sort.Direction.ASC
)

fun UserSort.toSort() = Sort.by(direction, field.filedName)
