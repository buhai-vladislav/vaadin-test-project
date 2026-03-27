package com.testtask.ui.user.dto

import com.testtask.domains.user.dto.CreateUserRequest
import com.testtask.domains.user.dto.UpdateUserRequest

class UserFormData {
    var name: String = ""
    var email: String = ""
}

fun UserFormData.toCreateUserRequest() = CreateUserRequest(
    name = this.name.trim(),
    email = this.email.trim(),
)

fun UserFormData.toUpdateUserRequest() = UpdateUserRequest(
    name = this.name.trim(),
    email = this.email.trim(),
)
