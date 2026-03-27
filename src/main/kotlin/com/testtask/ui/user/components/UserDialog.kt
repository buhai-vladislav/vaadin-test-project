package com.testtask.ui.user.components

import com.testtask.common.handlers.UiExceptionHandler
import com.testtask.domains.user.dto.UserResponseDto
import com.testtask.domains.user.service.UserService
import com.testtask.ui.common.notifications.Notifications
import com.testtask.ui.user.components.UserActions.buildUserActions
import com.testtask.ui.user.dto.UserFormData
import com.testtask.ui.user.dto.toCreateUserRequest
import com.testtask.ui.user.dto.toUpdateUserRequest
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.BeanValidationBinder
import com.vaadin.flow.data.validator.EmailValidator

class UserDialog(
    private val userService: UserService,
    private val userId: Long?,
    initialUser: UserResponseDto?,
    private val onSuccess: () -> Unit
) : Dialog() {
    private val nameField = TextField("Name")
    private val emailField = EmailField("Email")
    private val binder = BeanValidationBinder(UserFormData::class.java)
    private val formData = UserFormData()

    init {
        headerTitle = if (userId == null) "Create User" else "Edit User"
        width = "420px"

        configureFields()

        configureBinder()
        populateInitialData(initialUser)

        add(
            buildFormLayout(),
            buildUserActions(::onSave, ::close)
        )
    }

    private fun buildFormLayout() = FormLayout().apply {
        add(nameField, emailField)
    }

    private fun configureBinder() {
        binder.forField(nameField)
            .asRequired("Name is required")
            .bind(
                { it.name },
                { bean, value -> bean.name = value }
            )

        binder.forField(emailField)
            .asRequired("Email is required")
            .withValidator(EmailValidator("Enter a valid email"))
            .bind(
                { it.email },
                { bean, value -> bean.email = value }
            )
        binder.bean = formData
    }
    private fun configureFields() {
        nameField.isClearButtonVisible = true

        emailField.isClearButtonVisible = true
        emailField.errorMessage = "Enter a valid email"
    }

    private fun populateInitialData(initialUser: UserResponseDto?) {
        if (initialUser != null) {
            formData.name = initialUser.name
            formData.email = initialUser.email
        }

        binder.readBean(formData)
    }

    private fun onSave() {
        try {
            binder.writeBean(formData)

            when (userId) {
                null -> createUser()
                else -> updateUser()
            }

            close()
            onSuccess()
        } catch (e: Exception) {
            UiExceptionHandler.handle(e)
        }
    }

    private fun createUser() {
        userService.createUser(formData.toCreateUserRequest())
        Notifications.showSuccess("User created successfully")
    }

    private fun updateUser() {
        if (userId == null) return

        userService.updateUser(userId, formData.toUpdateUserRequest())
        Notifications.showSuccess("User updated successfully")
    }
}