package com.testtask.domains.user.service

import com.ninjasquad.springmockk.MockkBean
import com.testtask.base.BaseIntegrationTest
import com.testtask.domains.actionLogs.service.UserActionLogService
import com.testtask.domains.user.dto.CreateUserRequest
import com.testtask.domains.user.dto.FindUsersParams
import com.testtask.domains.user.dto.UpdateUserRequest
import com.testtask.domains.user.dto.UserFilter
import com.testtask.domains.user.dto.UserSort
import com.testtask.domains.user.exceptions.DuplicateEmailException
import com.testtask.domains.user.exceptions.UserNotFoundException
import com.testtask.domains.user.mock.UserGenerators.generateUserEntity
import com.testtask.domains.user.repository.UserRepository
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser

@Import(UserService::class)
class UserServiceIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var userActionLogService: UserActionLogService

    @BeforeEach
    fun setUp() {
        clearMocks(userActionLogService)
        every { userActionLogService.logCreate(any()) } just runs
        every { userActionLogService.logUpdate(any(), any()) } just runs
        every { userActionLogService.logDelete(any()) } just runs

        userRepository.deleteAll()
        userRepository.saveAll(
            listOf(
                generateUserEntity(id = null, name = "John Smith", email = "john.smith@example.com"),
                generateUserEntity(id = null, name = "Johnny Walker", email = "walker@example.com"),
                generateUserEntity(id = null, name = "Alice Brown", email = "alice@example.com"),
                generateUserEntity(id = null, name = "Bob Stone", email = "bob.s@example.com")
            )
        )
    }

    @Nested
    inner class FindAllUsers {

        @Test
        fun `should return paginated users`() {
            val result = userService.findAllUsers(
                FindUsersParams(
                    filter = UserFilter(name = null, email = null),
                    sort = UserSort(),
                    page = 0,
                    size = 2
                )
            )

            assertThat(result.page).isEqualTo(0)
            assertThat(result.size).isEqualTo(2)
            assertThat(result.totalItems).isEqualTo(4)
            assertThat(result.totalPages).isEqualTo(2)
            assertThat(result.items).hasSize(2)
        }

        @Test
        fun `should filter by name ignoring case`() {
            val result = userService.findAllUsers(
                FindUsersParams(
                    filter = UserFilter(name = "joHN", email = null),
                    sort = UserSort(),
                    page = 0,
                    size = 10
                )
            )

            assertThat(result.items.map { it.name })
                .containsExactlyInAnyOrder("John Smith", "Johnny Walker")
            assertThat(result.totalItems).isEqualTo(2)
        }

        @Test
        fun `should filter by email ignoring case`() {
            val result = userService.findAllUsers(
                FindUsersParams(
                    filter = UserFilter(name = null, email = "ALICE@EXAMPLE"),
                    sort = UserSort(),
                    page = 0,
                    size = 10
                )
            )

            assertThat(result.items.map { it.email })
                .containsExactly("alice@example.com")
            assertThat(result.totalItems).isEqualTo(1)
        }

        @Test
        fun `should combine filters with and`() {
            val result = userService.findAllUsers(
                FindUsersParams(
                    filter = UserFilter(name = "john", email = "smith@example"),
                    sort = UserSort(),
                    page = 0,
                    size = 10
                )
            )

            assertThat(result.items.map { it.name })
                .containsExactly("John Smith")
            assertThat(result.totalItems).isEqualTo(1)
        }

        @Test
        fun `should return empty result when nothing matches`() {
            val result = userService.findAllUsers(
                FindUsersParams(
                    filter = UserFilter(name = "missing", email = "missing"),
                    sort = UserSort(),
                    page = 0,
                    size = 10
                )
            )

            assertThat(result.items).isEmpty()
            assertThat(result.totalItems).isEqualTo(0)
            assertThat(result.totalPages).isEqualTo(0)
        }
    }

    @Nested
    @WithMockUser(roles = ["ADMIN"])
    inner class CreateUser {

        @Test
        fun `should create user and log create`() {
            val result = userService.createUser(
                CreateUserRequest(
                    name = "New User",
                    email = "new.user@example.com"
                )
            )

            val savedUser = userRepository.findAll()
                .firstOrNull { it.email == "new.user@example.com" }

            assertThat(result.id).isNotNull
            assertThat(result.name).isEqualTo("New User")
            assertThat(result.email).isEqualTo("new.user@example.com")
            assertThat(savedUser).isNotNull
            assertThat(userRepository.count()).isEqualTo(5)

            verify(exactly = 1) {
                userActionLogService.logCreate(
                    withArg {
                        assertThat(it.id).isNotNull
                        assertThat(it.name).isEqualTo("New User")
                        assertThat(it.email).isEqualTo("new.user@example.com")
                    }
                )
            }
        }

        @Test
        fun `should throw DuplicateEmailException when email already exists`() {
            assertThatThrownBy {
                userService.createUser(
                    CreateUserRequest(
                        name = "Duplicated User",
                        email = "john.smith@example.com"
                    )
                )
            }.isInstanceOf(DuplicateEmailException::class.java)

            assertThat(userRepository.count()).isEqualTo(4)
            verify(exactly = 0) { userActionLogService.logCreate(any()) }
        }

        @Test
        fun `should trim email before duplicate check`() {
            assertThatThrownBy {
                userService.createUser(
                    CreateUserRequest(
                        name = "Duplicated User",
                        email = "  john.smith@example.com  "
                    )
                )
            }.isInstanceOf(DuplicateEmailException::class.java)

            assertThat(userRepository.count()).isEqualTo(4)
            verify(exactly = 0) { userActionLogService.logCreate(any()) }
        }
    }

    @Nested
    @WithMockUser(roles = ["ADMIN"])
    inner class UpdateUser {

        @Test
        fun `should update user and log update`() {
            val existingUser = userRepository.findAll()
                .first { it.email == "john.smith@example.com" }

            val result = userService.updateUser(
                existingUser.id!!,
                UpdateUserRequest(
                    name = "John Updated",
                    email = "john.updated@example.com"
                )
            )

            val updatedUser = userRepository.findById(existingUser.id!!).orElseThrow()

            assertThat(result.id).isEqualTo(existingUser.id)
            assertThat(result.name).isEqualTo("John Updated")
            assertThat(result.email).isEqualTo("john.updated@example.com")
            assertThat(updatedUser.name).isEqualTo("John Updated")
            assertThat(updatedUser.email).isEqualTo("john.updated@example.com")

            verify(exactly = 1) {
                userActionLogService.logUpdate(
                    withArg {
                        assertThat(it.id).isEqualTo(existingUser.id)
                        assertThat(it.name).isEqualTo("John Smith")
                        assertThat(it.email).isEqualTo("john.smith@example.com")
                    },
                    withArg {
                        assertThat(it.id).isEqualTo(existingUser.id)
                        assertThat(it.name).isEqualTo("John Updated")
                        assertThat(it.email).isEqualTo("john.updated@example.com")
                    }
                )
            }
        }

        @Test
        fun `should throw UserNotFoundException when updating missing user`() {
            assertThatThrownBy {
                userService.updateUser(
                    999999L,
                    UpdateUserRequest(
                        name = "Missing",
                        email = "missing@example.com"
                    )
                )
            }.isInstanceOf(UserNotFoundException::class.java)

            verify(exactly = 0) { userActionLogService.logUpdate(any(), any()) }
        }

        @Test
        fun `should throw DuplicateEmailException when updating to existing email`() {
            val existingUser = userRepository.findAll()
                .first { it.email == "john.smith@example.com" }

            assertThatThrownBy {
                userService.updateUser(
                    existingUser.id!!,
                    UpdateUserRequest(
                        name = "John Updated",
                        email = "alice@example.com"
                    )
                )
            }.isInstanceOf(DuplicateEmailException::class.java)

            val unchangedUser = userRepository.findById(existingUser.id!!).orElseThrow()
            assertThat(unchangedUser.name).isEqualTo("John Smith")
            assertThat(unchangedUser.email).isEqualTo("john.smith@example.com")

            verify(exactly = 0) { userActionLogService.logUpdate(any(), any()) }
        }

        @Test
        fun `should trim email before duplicate check on update`() {
            val existingUser = userRepository.findAll()
                .first { it.email == "john.smith@example.com" }

            assertThatThrownBy {
                userService.updateUser(
                    existingUser.id!!,
                    UpdateUserRequest(
                        name = "John Updated",
                        email = "  alice@example.com  "
                    )
                )
            }.isInstanceOf(DuplicateEmailException::class.java)

            verify(exactly = 0) { userActionLogService.logUpdate(any(), any()) }
        }
    }

    @Nested
    @WithMockUser(roles = ["ADMIN"])
    inner class DeleteUser {

        @Test
        fun `should delete user and log delete`() {
            val existingUser = userRepository.findAll()
                .first { it.email == "john.smith@example.com" }

            userService.deleteUser(existingUser.id!!)

            assertThat(userRepository.findById(existingUser.id!!)).isEmpty
            assertThat(userRepository.count()).isEqualTo(3)

            verify(exactly = 1) {
                userActionLogService.logDelete(
                    withArg {
                        assertThat(it.id).isEqualTo(existingUser.id)
                        assertThat(it.name).isEqualTo("John Smith")
                        assertThat(it.email).isEqualTo("john.smith@example.com")
                    }
                )
            }
        }

        @Test
        fun `should throw UserNotFoundException when deleting missing user`() {
            assertThatThrownBy {
                userService.deleteUser(999999L)
            }.isInstanceOf(UserNotFoundException::class.java)

            assertThat(userRepository.count()).isEqualTo(4)
            verify(exactly = 0) { userActionLogService.logDelete(any()) }
        }
    }
}