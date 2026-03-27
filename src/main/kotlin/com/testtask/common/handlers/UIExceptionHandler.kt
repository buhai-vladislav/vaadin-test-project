package com.testtask.common.handlers

import com.testtask.domains.user.exceptions.DuplicateEmailException
import com.testtask.domains.user.exceptions.UserNotFoundException
import com.testtask.ui.common.notifications.Notifications.showError

object UiExceptionHandler {

    fun handle(exception: Throwable) {
        when (exception) {
            is DuplicateEmailException -> showError(exception.message ?: "Email already exists")
            is UserNotFoundException -> showError(exception.message ?: "User not found")
            else -> showError("Unexpected error occurred")
        }
    }
}