package com.testtask.domains.user.dto

import com.testtask.domains.user.entity.UserEntity
import java.time.LocalDateTime

data class UpdateUserRequest(
    val name: String,
    val email: String,
)

fun UpdateUserRequest.toUserEntity(existing: UserEntity): UserEntity {
    return UserEntity(
        id = existing.id,
        name = this.name.trim(),
        email = this.email.trim(),
        createdAt = existing.createdAt,
        updatedAt = LocalDateTime.now(),
    )
}
