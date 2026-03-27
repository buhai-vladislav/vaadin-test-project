package com.testtask.domains.actionLogs.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.testtask.base.BaseIntegrationTest
import com.testtask.domains.actionLogs.enums.UserAction
import com.testtask.domains.actionLogs.repository.UserActionLogRepository
import com.testtask.domains.user.mock.UserGenerators.generateUserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser

@Import(UserActionLogService::class)
class UserActionLogServiceIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var userActionLogService: UserActionLogService

    @Autowired
    private lateinit var userActionLogRepository: UserActionLogRepository

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        userActionLogRepository.deleteAll()
        objectMapper = ObjectMapper()
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `should save create log`() {
        val user = generateUserEntity(
            id = 10L,
            name = "John Smith",
            email = "john.smith@example.com"
        )

        userActionLogService.logCreate(user)

        val logs = userActionLogRepository.findAll()

        assertThat(logs).hasSize(1)

        val log = logs.first()

        assertThat(log.userId).isEqualTo(10L)
        assertThat(log.action).isEqualTo(UserAction.CREATE_USER)
        assertThat(log.performedBy).isEqualTo("admin@test.com")
        assertThat(log.performedAt).isNotNull
        assertThat(log.details).isNotBlank
        assertThat(log.details).contains("John Smith")
        assertThat(log.details).contains("john.smith@example.com")
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `should save update log`() {
        val before = generateUserEntity(
            id = 15L,
            name = "John Smith",
            email = "john.smith@example.com"
        )

        val after = generateUserEntity(
            id = 15L,
            name = "John Updated",
            email = "john.updated@example.com"
        )

        userActionLogService.logUpdate(before, after)

        val logs = userActionLogRepository.findAll()

        assertThat(logs).hasSize(1)

        val log = logs.first()

        assertThat(log.userId).isEqualTo(15L)
        assertThat(log.action).isEqualTo(UserAction.UPDATE_USER)
        assertThat(log.performedBy).isEqualTo("admin@test.com")
        assertThat(log.performedAt).isNotNull
        assertThat(log.details).isNotBlank
        assertThat(log.details).contains("John Smith")
        assertThat(log.details).contains("john.smith@example.com")
        assertThat(log.details).contains("John Updated")
        assertThat(log.details).contains("john.updated@example.com")
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `should save delete log`() {
        val user = generateUserEntity(
            id = 20L,
            name = "Alice Brown",
            email = "alice@example.com"
        )

        userActionLogService.logDelete(user)

        val logs = userActionLogRepository.findAll()

        assertThat(logs).hasSize(1)

        val log = logs.first()

        assertThat(log.userId).isEqualTo(20L)
        assertThat(log.action).isEqualTo(UserAction.DELETE_USER)
        assertThat(log.performedBy).isEqualTo("admin@test.com")
        assertThat(log.performedAt).isNotNull
        assertThat(log.details).isNotBlank

        assertThat(log.details).contains("Alice Brown")
        assertThat(log.details).contains("alice@example.com")
    }

    @Test
    fun `should save log with unknown performer when authentication is absent`() {
        val user = generateUserEntity(
            id = 25L,
            name = "Bob Stone",
            email = "bob@example.com"
        )

        userActionLogService.logCreate(user)

        val logs = userActionLogRepository.findAll()

        assertThat(logs).hasSize(1)

        val log = logs.first()

        assertThat(log.userId).isEqualTo(25L)
        assertThat(log.action).isEqualTo(UserAction.CREATE_USER)
        assertThat(log.performedBy).isEqualTo("unknown")
        assertThat(log.performedAt).isNotNull
    }
}