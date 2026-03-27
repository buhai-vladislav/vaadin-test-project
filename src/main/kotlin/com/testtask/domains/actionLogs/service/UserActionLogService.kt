package com.testtask.domains.actionLogs.service

import com.testtask.base.BaseService
import com.testtask.domains.actionLogs.dto.toUserActionSnapshot
import com.testtask.domains.actionLogs.entity.UserActionLogEntity
import com.testtask.domains.actionLogs.enums.UserAction
import com.testtask.domains.actionLogs.repository.UserActionLogRepository
import com.testtask.domains.user.entity.UserEntity
import com.testtask.security.SecurityUtils
import com.vaadin.uitest.parser.Parser.objectMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserActionLogService(
    private val userAuditLogRepository: UserActionLogRepository
) : BaseService(UserActionLogService::class.java) {
    fun logCreate(user: UserEntity) {
        saveLog(
            userId = user.id,
            action = UserAction.CREATE_USER,
            details = mapOf(
                "entity" to user.toUserActionSnapshot()
            )
        )
    }

    fun logUpdate(
        before: UserEntity,
        after: UserEntity
    ) {
        saveLog(
            userId = after.id,
            action = UserAction.UPDATE_USER,
            details = mapOf(
                "before" to before.toUserActionSnapshot(),
                "after" to after.toUserActionSnapshot()
            )
        )
    }

    fun logDelete(user: UserEntity) {
        saveLog(
            userId = user.id,
            action = UserAction.DELETE_USER,
            details = mapOf(
                "entity" to user.toUserActionSnapshot()
            )
        )
    }

    private fun saveLog(
        userId: Long?,
        action: UserAction,
        details: Any
    ) {
        try {
            val performedBy = SecurityUtils.username() ?: "unknown"
            val entity = UserActionLogEntity(
                userId = userId,
                action = action,
                performedBy = performedBy,
                performedAt = LocalDateTime.now(),
                details = objectMapper.writeValueAsString(details)
            )

            userAuditLogRepository.save(entity)
            logger.info("Action log has been successfully created")
        } catch (ex: Exception) {
            logger.error("Failed to save user action log for userId=$userId, action=$action", ex)
        }
    }
}