package com.testtask.domains.user.repository

import com.testtask.domains.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface UserRepository : JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    fun existsByEmailIgnoreCase(email: String): Boolean
    fun existsByEmailIgnoreCaseAndIdNot(email: String, id: Long): Boolean
}