package com.testtask.domains.user.service

import com.testtask.common.dto.PaginationResult
import com.testtask.domains.actionLogs.service.UserActionLogService
import com.testtask.domains.user.dto.CreateUserRequest
import com.testtask.domains.user.dto.FindUsersParams
import com.testtask.domains.user.dto.UpdateUserRequest
import com.testtask.domains.user.dto.UserFilter
import com.testtask.domains.user.dto.UserResponseDto
import com.testtask.domains.user.dto.UserSort
import com.testtask.domains.user.entity.UserEntity
import com.testtask.domains.user.exceptions.DuplicateEmailException
import com.testtask.domains.user.exceptions.UserNotFoundException
import com.testtask.domains.user.mock.UserGenerators.generateUserEntity
import com.testtask.domains.user.repository.UserRepository
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import java.util.Optional

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userActionLogService: UserActionLogService
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        userActionLogService = mockk()
        userService = UserService(
            userRepository = userRepository,
            userActionLogService = userActionLogService
        )
    }

    @Nested
    inner class FindAllUsers {

        @Test
        fun `should return paginated users`() {
            val params = FindUsersParams(
                filter = UserFilter(
                    name = "john",
                    email = "example.com"
                ),
                sort = UserSort(),
                page = 0,
                size = 2
            )

            val user1 = generateUserEntity(
                id = 1L,
                name = "John Smith",
                email = "john.smith@example.com"
            )

            val user2 = generateUserEntity(
                id = 2L,
                name = "Johnny Walker",
                email = "johnny.walker@example.com"
            )

            val pageableSlot = slot<PageRequest>()

            every {
                userRepository.findAll(
                    any<Specification<UserEntity>>(),
                    capture(pageableSlot)
                )
            } returns PageImpl(
                listOf(user1, user2),
                PageRequest.of(0, 2),
                5
            )

            val result = userService.findAllUsers(params)

            assertThat(result.page).isEqualTo(0)
            assertThat(result.size).isEqualTo(2)
            assertThat(result.totalItems).isEqualTo(5)
            assertThat(result.totalPages).isEqualTo(3)
            assertThat(result.items).hasSize(2)
            assertThat(pageableSlot.captured.pageNumber).isEqualTo(0)
            assertThat(pageableSlot.captured.pageSize).isEqualTo(2)

            verify(exactly = 1) {
                userRepository.findAll(any<Specification<UserEntity>>(), any<PageRequest>())
            }
            confirmVerified(userRepository, userActionLogService)
        }

        @Test
        fun `should return empty pagination result when repository returns empty page`() {
            val params = FindUsersParams(
                filter = UserFilter(
                    name = null,
                    email = null
                ),
                sort = UserSort(),
                page = 0,
                size = 10
            )

            every {
                userRepository.findAll(
                    any<Specification<UserEntity>>(),
                    any<PageRequest>()
                )
            } returns PageImpl(
                emptyList(),
                PageRequest.of(0, 10),
                0
            )

            val result = userService.findAllUsers(params)

            assertThat(result).isEqualTo(
                PaginationResult(
                    items = emptyList<UserResponseDto>(),
                    page = 0,
                    size = 10,
                    totalItems = 0,
                    totalPages = 0
                )
            )

            verify(exactly = 1) {
                userRepository.findAll(any<Specification<UserEntity>>(), any<PageRequest>())
            }
            confirmVerified(userRepository, userActionLogService)
        }
    }

    @Nested
    inner class CreateUser {

        @Test
        fun `should create user and log create`() {
            val request = CreateUserRequest(
                name = "John Smith",
                email = "john.smith@example.com"
            )

            val savedUser = generateUserEntity(
                id = 1L,
                name = "John Smith",
                email = "john.smith@example.com"
            )

            every { userRepository.existsByEmailIgnoreCase("john.smith@example.com") } returns false
            every { userRepository.save(any<UserEntity>()) } returns savedUser
            every { userActionLogService.logCreate(savedUser) } just runs

            val result = userService.createUser(request)

            assertThat(result.id).isEqualTo(1L)
            assertThat(result.name).isEqualTo("John Smith")
            assertThat(result.email).isEqualTo("john.smith@example.com")

            verify(exactly = 1) { userRepository.existsByEmailIgnoreCase("john.smith@example.com") }
            verify(exactly = 1) { userRepository.save(any<UserEntity>()) }
            verify(exactly = 1) { userActionLogService.logCreate(savedUser) }
            confirmVerified(userRepository, userActionLogService)
        }

        @Test
        fun `should trim email before duplicate check on create`() {
            val request = CreateUserRequest(
                name = "John Smith",
                email = "  john.smith@example.com  "
            )

            val savedUser = generateUserEntity(
                id = 1L,
                name = "John Smith",
                email = "john.smith@example.com"
            )

            every { userRepository.existsByEmailIgnoreCase("john.smith@example.com") } returns false
            every { userRepository.save(any<UserEntity>()) } returns savedUser
            every { userActionLogService.logCreate(savedUser) } just runs

            userService.createUser(request)

            verify(exactly = 1) { userRepository.existsByEmailIgnoreCase("john.smith@example.com") }
            verify(exactly = 1) { userRepository.save(any<UserEntity>()) }
            verify(exactly = 1) { userActionLogService.logCreate(savedUser) }
            confirmVerified(userRepository, userActionLogService)
        }

        @Test
        fun `should throw DuplicateEmailException when email already exists on create`() {
            val request = CreateUserRequest(
                name = "John Smith",
                email = "john.smith@example.com"
            )

            every { userRepository.existsByEmailIgnoreCase("john.smith@example.com") } returns true

            assertThatThrownBy {
                userService.createUser(request)
            }.isInstanceOf(DuplicateEmailException::class.java)

            verify(exactly = 1) { userRepository.existsByEmailIgnoreCase("john.smith@example.com") }
            verify(exactly = 0) { userRepository.save(any<UserEntity>()) }
            verify(exactly = 0) { userActionLogService.logCreate(any()) }
            confirmVerified(userRepository, userActionLogService)
        }
    }

    @Nested
    inner class UpdateUser {

        @Test
        fun `should update user and log update`() {
            val request = UpdateUserRequest(
                name = "John Updated",
                email = "john.updated@example.com"
            )

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
            every {
                userRepository.existsByEmailIgnoreCaseAndIdNot(
                    "john.updated@example.com",
                    1L
                )
            } returns false
            every { userRepository.save(any<UserEntity>()) } returns updatedUser
            every {
                userActionLogService.logUpdate(
                    before = existingUser,
                    after = updatedUser
                )
            } just runs

            val result = userService.updateUser(1L, request)

            assertThat(result.id).isEqualTo(1L)
            assertThat(result.name).isEqualTo("John Updated")
            assertThat(result.email).isEqualTo("john.updated@example.com")

            verify(exactly = 1) { userRepository.findById(1L) }
            verify(exactly = 1) {
                userRepository.existsByEmailIgnoreCaseAndIdNot(
                    "john.updated@example.com",
                    1L
                )
            }
            verify(exactly = 1) { userRepository.save(any<UserEntity>()) }
            verify(exactly = 1) {
                userActionLogService.logUpdate(
                    before = existingUser,
                    after = updatedUser
                )
            }
            confirmVerified(userRepository, userActionLogService)
        }

        @Test
        fun `should trim email before duplicate check on update`() {
            val request = UpdateUserRequest(
                name = "John Updated",
                email = "  john.updated@example.com  "
            )

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
            every {
                userRepository.existsByEmailIgnoreCaseAndIdNot(
                    "john.updated@example.com",
                    1L
                )
            } returns false
            every { userRepository.save(any<UserEntity>()) } returns updatedUser
            every {
                userActionLogService.logUpdate(
                    before = existingUser,
                    after = updatedUser
                )
            } just runs

            userService.updateUser(1L, request)

            verify(exactly = 1) {
                userRepository.existsByEmailIgnoreCaseAndIdNot(
                    "john.updated@example.com",
                    1L
                )
            }
            verify(exactly = 1) { userRepository.findById(1L) }
            verify(exactly = 1) { userRepository.save(any<UserEntity>()) }
            verify(exactly = 1) {
                userActionLogService.logUpdate(
                    before = existingUser,
                    after = updatedUser
                )
            }
            confirmVerified(userRepository, userActionLogService)
        }

        @Test
        fun `should throw UserNotFoundException when user does not exist on update`() {
            val request = UpdateUserRequest(
                name = "John Updated",
                email = "john.updated@example.com"
            )

            every { userRepository.findById(1L) } returns Optional.empty()

            assertThatThrownBy {
                userService.updateUser(1L, request)
            }.isInstanceOf(UserNotFoundException::class.java)

            verify(exactly = 1) { userRepository.findById(1L) }
            verify(exactly = 0) {
                userRepository.existsByEmailIgnoreCaseAndIdNot(any(), any())
            }
            verify(exactly = 0) { userRepository.save(any<UserEntity>()) }
            verify(exactly = 0) { userActionLogService.logUpdate(any(), any()) }
            confirmVerified(userRepository, userActionLogService)
        }

        @Test
        fun `should throw DuplicateEmailException when another user already has email on update`() {
            val request = UpdateUserRequest(
                name = "John Updated",
                email = "john.updated@example.com"
            )

            val existingUser = generateUserEntity(
                id = 1L,
                name = "John Smith",
                email = "john.smith@example.com"
            )

            every { userRepository.findById(1L) } returns Optional.of(existingUser)
            every {
                userRepository.existsByEmailIgnoreCaseAndIdNot(
                    "john.updated@example.com",
                    1L
                )
            } returns true

            assertThatThrownBy {
                userService.updateUser(1L, request)
            }.isInstanceOf(DuplicateEmailException::class.java)

            verify(exactly = 1) { userRepository.findById(1L) }
            verify(exactly = 1) {
                userRepository.existsByEmailIgnoreCaseAndIdNot(
                    "john.updated@example.com",
                    1L
                )
            }
            verify(exactly = 0) { userRepository.save(any<UserEntity>()) }
            verify(exactly = 0) { userActionLogService.logUpdate(any(), any()) }
            confirmVerified(userRepository, userActionLogService)
        }
    }

    @Nested
    inner class DeleteUser {

        @Test
        fun `should delete user and log delete`() {
            val user = generateUserEntity(
                id = 1L,
                name = "John Smith",
                email = "john.smith@example.com"
            )

            every { userRepository.findById(1L) } returns Optional.of(user)
            every { userRepository.deleteById(1L) } just runs
            every { userActionLogService.logDelete(user) } just runs

            userService.deleteUser(1L)

            verify(exactly = 1) { userRepository.findById(1L) }
            verify(exactly = 1) { userRepository.deleteById(1L) }
            verify(exactly = 1) { userActionLogService.logDelete(user) }
            confirmVerified(userRepository, userActionLogService)
        }

        @Test
        fun `should throw UserNotFoundException when user does not exist on delete`() {
            every { userRepository.findById(1L) } returns Optional.empty()

            assertThatThrownBy {
                userService.deleteUser(1L)
            }.isInstanceOf(UserNotFoundException::class.java)

            verify(exactly = 1) { userRepository.findById(1L) }
            verify(exactly = 0) { userRepository.deleteById(any()) }
            verify(exactly = 0) { userActionLogService.logDelete(any()) }
            confirmVerified(userRepository, userActionLogService)
        }
    }
}