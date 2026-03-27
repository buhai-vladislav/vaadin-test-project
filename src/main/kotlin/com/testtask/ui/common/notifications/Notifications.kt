package com.testtask.ui.common.notifications

import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant

object Notifications {
    fun showSuccess(message: String) {
        Notification.show(message, 3000, Notification.Position.TOP_END)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS)
    }

    fun showError(message: String) {
        Notification.show(message, 4000, Notification.Position.TOP_END)
            .addThemeVariants(NotificationVariant.LUMO_ERROR)
    }
}