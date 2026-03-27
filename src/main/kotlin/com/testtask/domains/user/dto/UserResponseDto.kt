package com.testtask.domains.user.dto

import com.testtask.domains.user.entity.UserEntity

data class UserResponseDto(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: String,
    val updatedAt: String,
)

fun UserEntity.toUserResponseDto() = UserResponseDto(
    id = this.id!!,
    name = this.name,
    email = this.email,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString(),
)
