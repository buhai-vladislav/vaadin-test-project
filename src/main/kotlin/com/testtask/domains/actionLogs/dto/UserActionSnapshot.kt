package com.testtask.domains.actionLogs.dto

import com.testtask.domains.user.entity.UserEntity

data class UserActionSnapshot(
    val id: Long?,
    val name: String,
    val email: String,
    val createdAt: String,
    val updatedAt: String
)

fun UserEntity.toUserActionSnapshot() = UserActionSnapshot(
    id = this.id,
    name = this.name,
    email = this.email,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString()
)
