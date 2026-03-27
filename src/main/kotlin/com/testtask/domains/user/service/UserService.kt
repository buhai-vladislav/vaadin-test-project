package com.testtask.domains.user.service

import com.testtask.base.BaseService
import com.testtask.common.dto.PaginationResult
import com.testtask.domains.actionLogs.service.UserActionLogService
import com.testtask.domains.user.dto.CreateUserRequest
import com.testtask.domains.user.dto.FindUsersParams
import com.testtask.domains.user.dto.UpdateUserRequest
import com.testtask.domains.user.dto.toSort
import com.testtask.domains.user.dto.toUserEntity
import com.testtask.domains.user.dto.toUserResponseDto
import com.testtask.domains.user.exceptions.DuplicateEmailException
import com.testtask.domains.user.exceptions.UserNotFoundException
import com.testtask.domains.user.repository.UserRepository
import com.testtask.domains.user.repository.UserSpecifications
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userActionLogService: UserActionLogService
) : BaseService(UserService::class.java) {

    fun findAllUsers(params: FindUsersParams) = executeWithLogging("findAllUsers") {
        val specification = UserSpecifications.withFilter(params.filter)
        val sort = params.sort.toSort()
        val pageable = PageRequest.of(params.page, params.size, sort)

        val users = userRepository.findAll(specification, pageable)

        PaginationResult(
            items = users.content.map { it.toUserResponseDto() },
            page = users.number,
            size = users.size,
            totalItems = users.totalElements,
            totalPages = users.totalPages
        )
    }

    @PreAuthorize("hasRole('ADMIN')")
    fun createUser(request: CreateUserRequest) = executeWithLogging("createUser") {
        if (userRepository.existsByEmailIgnoreCase(request.email.trim())) {
            throw DuplicateEmailException(request.email)
        }

        val userEntity = request.toUserEntity()
        val savedUser = userRepository.save(userEntity).also {
            userActionLogService.logCreate(it)
        }

        savedUser.toUserResponseDto()
    }

    @PreAuthorize("hasRole('ADMIN')")
    fun updateUser(id: Long, request: UpdateUserRequest) = executeWithLogging("updateUser") {
        val userEntity = userRepository.findById(id).orElseThrow {
            UserNotFoundException(id)
        }

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.email.trim(), userEntity.id!!)) {
            throw DuplicateEmailException(request.email)
        }

        val updatedEntity = request.toUserEntity(userEntity)
        val beforeUpdate = userEntity.copy()

        val savedUser = userRepository.save(updatedEntity).also {
            userActionLogService.logUpdate(before = beforeUpdate, after = it)
        }

        savedUser.toUserResponseDto()
    }

    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(id: Long) = executeWithLogging("deleteUser") {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException(id) }

        userRepository.deleteById(id).also {
            userActionLogService.logDelete(user)
        }
    }
}