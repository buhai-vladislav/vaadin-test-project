package com.testtask.domains.user.service

import com.ninjasquad.springmockk.MockkBean
import com.testtask.base.BaseIntegrationTest
import com.testtask.domains.actionLogs.service.UserActionLogService
import com.testtask.domains.user.dto.CreateUserRequest
import com.testtask.domains.user.dto.UpdateUserRequest
import com.testtask.domains.user.mock.UserGenerators.generateUserEntity
import com.testtask.domains.user.repository.UserRepository
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.test.context.support.WithMockUser
import java.util.Optional

@Import(UserService::class)
class UserServiceSecurityTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var userActionLogService: UserActionLogService

    @Test
    fun `should deny create user for unauthenticated user`() {
        assertThatThrownBy {
            userService.createUser(
                CreateUserRequest(
                    name = "John Smith",
                    email = "john.smith@example.com"
                )
            )
        }.isInstanceOf(AuthenticationCredentialsNotFoundException::class.java)
    }

    @Test
    fun `should deny update user for unauthenticated user`() {
        assertThatThrownBy {
            userService.updateUser(
                1L,
                UpdateUserRequest(
                    name = "John Updated",
                    email = "john.updated@example.com"
                )
            )
        }.isInstanceOf(AuthenticationCredentialsNotFoundException::class.java)
    }

    @Test
    fun `should deny delete user for unauthenticated user`() {
        assertThatThrownBy {
            userService.deleteUser(1L)
        }.isInstanceOf(AuthenticationCredentialsNotFoundException::class.java)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `should deny create user for non admin`() {
        assertThatThrownBy {
            userService.createUser(
                CreateUserRequest(
                    name = "John Smith",
                    email = "john.smith@example.com"
                )
            )
        }.isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `should deny update user for non admin`() {
        assertThatThrownBy {
            userService.updateUser(
                1L,
                UpdateUserRequest(
                    name = "John Updated",
                    email = "john.updated@example.com"
                )
            )
        }.isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `should deny delete user for non admin`() {
        assertThatThrownBy {
            userService.deleteUser(1L)
        }.isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should allow create user for admin`() {
        val savedUser = generateUserEntity(
            id = 1L,
            name = "John Smith",
            email = "john.smith@example.com"
        )

        every { userRepository.existsByEmailIgnoreCase("john.smith@example.com") } returns false
        every { userRepository.save(any()) } returns savedUser
        every { userActionLogService.logCreate(any()) } just runs

        userService.createUser(
            CreateUserRequest(
                name = "John Smith",
                email = "john.smith@example.com"
            )
        )
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should allow update user for admin`() {
        val existingUser = generateUserEntity(
            id = 1L,
            name = "John Smith",
            email = "john.smith@example.com"
        )

        val updatedUser = generateUserEntity(
            id = 1L,
            name = "John Updated",
            email = "john.updated@example.com"
        )

        every { userRepository.findById(1L) } returns Optional.of(existingUser)
        every { userRepository.existsByEmailIgnoreCaseAndIdNot("john.updated@example.com", 1L) } returns false
        every { userRepository.save(any()) } returns updatedUser
        every { userActionLogService.logUpdate(any(), any()) } just runs

        userService.updateUser(
            1L,
            UpdateUserRequest(
                name = "John Updated",
                email = "john.updated@example.com"
            )
        )
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should allow delete user for admin`() {
        val existingUser = generateUserEntity(
            id = 1L,
            name = "John Smith",
            email = "john.smith@example.com"
        )

        every { userRepository.findById(1L) } returns Optional.of(existingUser)
        every { userRepository.deleteById(1L) } just runs
        every { userActionLogService.logDelete(any()) } just runs

        userService.deleteUser(1L)
    }
}