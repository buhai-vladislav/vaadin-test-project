package com.testtask.domains.user.repository

import com.testtask.base.BaseIntegrationTest
import com.testtask.domains.user.dto.UserFilter
import com.testtask.domains.user.mock.UserGenerators.generateUserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@DisplayName("Test user repository")

class UserSpecificationsTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        userRepository.saveAll(
            listOf(
                generateUserEntity(name = "John Smith", email = "john.smith@example.com"),
                generateUserEntity(name = "Johnny Walker", email = "walker@example.com"),
                generateUserEntity(name = "Alice Brown", email = "alice@example.com"),
                generateUserEntity(name = "Bob Stone", email = "bob.s@example.com")
            )
        )
    }

    @Test
    fun `test should return all users when filter is empty`() {
        val filter = UserFilter(name = null, email = null)

        val result = userRepository.findAll(UserSpecifications.withFilter(filter))

        assertThat(result).hasSize(4)
    }

    @Test
    fun `test should filter by name ignoring case`() {
        val filter = UserFilter(name = "joHN", email = null)

        val result = userRepository.findAll(UserSpecifications.withFilter(filter))

        assertThat(result.map { it.name })
            .containsExactlyInAnyOrder("John Smith", "Johnny Walker")
    }

    @Test
    fun `test should filter by email ignoring case`() {
        val filter = UserFilter(name = null, email = "ALICE@EXAMPLE")

        val result = userRepository.findAll(UserSpecifications.withFilter(filter))

        assertThat(result.map { it.email })
            .containsExactly("alice@example.com")
    }

    @Test
    fun `test should combine filters with and`() {
        val filter = UserFilter(name = "john", email = "smith@example")

        val result = userRepository.findAll(UserSpecifications.withFilter(filter))

        assertThat(result.map { it.name })
            .containsExactly("John Smith")
    }

    @Test
    fun `test should return empty list when nothing matches`() {
        val filter = UserFilter(name = "missing", email = "missing")

        val result = userRepository.findAll(UserSpecifications.withFilter(filter))

        assertThat(result).isEmpty()
    }

    @Test
    fun `test should ignore blank values`() {
        val filter = UserFilter(name = "   ", email = "bob.s@example.com")

        val result = userRepository.findAll(UserSpecifications.withFilter(filter))

        assertThat(result.map { it.name })
            .containsExactly("Bob Stone")
    }
}