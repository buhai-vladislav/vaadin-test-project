package com.testtask.domains.user.dto

import com.testtask.domains.user.entity.UserEntity
import java.time.LocalDateTime

data class CreateUserRequest(
    val name: String,
    val email: String,
)

fun CreateUserRequest.toUserEntity(): UserEntity {
    val now = LocalDateTime.now()

    return UserEntity(
        name = this.name,
        email = this.email,
        createdAt = now,
        updatedAt = now,
    )
}
