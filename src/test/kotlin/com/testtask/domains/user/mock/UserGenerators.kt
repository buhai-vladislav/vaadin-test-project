package com.testtask.domains.user.mock

import com.testtask.domains.user.entity.UserEntity
import java.time.LocalDateTime

object UserGenerators {
    fun generateUserEntity(
        id: Long? = null,
        name: String = "John Doe",
        email: String = "johndoe@gmail.com"
    ) : UserEntity {
        return UserEntity(
            id = id,
            name = name,
            email = email,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}