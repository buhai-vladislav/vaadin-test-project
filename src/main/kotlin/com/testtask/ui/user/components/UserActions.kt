package com.testtask.ui.user.components

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout

object UserActions {
    fun buildUserActions(onSave: () -> Unit, onClose: () -> Unit): HorizontalLayout {
        val saveButton = Button("Save") {
            onSave()

        }.apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        }

        val cancelButton = Button("Cancel") {
            onClose()
        }

        return HorizontalLayout(cancelButton, saveButton)
    }
}