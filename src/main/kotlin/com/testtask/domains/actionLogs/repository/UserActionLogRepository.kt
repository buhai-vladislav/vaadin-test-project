package com.testtask.domains.actionLogs.repository

import com.testtask.domains.actionLogs.entity.UserActionLogEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserActionLogRepository : JpaRepository<UserActionLogEntity, Long>