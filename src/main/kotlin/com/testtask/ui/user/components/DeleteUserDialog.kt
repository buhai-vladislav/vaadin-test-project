package com.testtask.ui.user.components

import com.testtask.domains.user.exceptions.UserNotFoundException
import com.testtask.domains.user.service.UserService
import com.testtask.ui.common.notifications.Notifications
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.HorizontalLayout

class DeleteUserDialog(
    private val userService: UserService,
    private val userId: Long,
    userName: String,
    private val onSuccess: () -> Unit
) : Dialog() {

    init {
        headerTitle = "Delete User"
        width = "420px"

        add(Paragraph("Are you sure you want to delete '$userName'?"))
        add(buildButtons(userName))
    }


    private fun buildButtons(userName: String): HorizontalLayout {
        val cancelButton = Button("Cancel") { close() }

        val deleteButton = Button("Delete") {
            onDelete(userName)
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY)
        }

        return HorizontalLayout(cancelButton, deleteButton)
    }

    private fun onDelete(userName: String) {
        try {
            userService.deleteUser(userId).also { Notifications.showSuccess("User $userName deleted!") }

            close()
            onSuccess()
        } catch (e: UserNotFoundException) {
            Notifications.showError(e.message ?: "User $userName not found")
        } catch (e: Exception) {
            Notifications.showError(e.message ?: "Failed to delete $userName user")
        }
    }
}