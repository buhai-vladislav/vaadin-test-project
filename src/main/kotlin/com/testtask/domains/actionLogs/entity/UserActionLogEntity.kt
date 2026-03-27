package com.testtask.domains.actionLogs.entity

import com.testtask.domains.actionLogs.enums.UserAction
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "user_action_logs")
data class UserActionLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id")
    var userId: Long?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var action: UserAction,

    @Column(name = "performed_by", nullable = false)
    var performedBy: String,

    @Column(name = "performed_at", nullable = false)
    var performedAt: LocalDateTime,

    @Column(nullable = false, columnDefinition = "TEXT")
    var details: String
)
