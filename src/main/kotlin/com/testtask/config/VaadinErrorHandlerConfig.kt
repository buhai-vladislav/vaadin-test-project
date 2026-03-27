package com.testtask.config

import com.testtask.ui.common.notifications.Notifications
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.ErrorEvent
import com.vaadin.flow.server.ErrorHandler
import org.springframework.stereotype.Component

@Component
class VaadinErrorHandlerConfig : ErrorHandler {

    override fun error(event: ErrorEvent) {
        val throwable = event.throwable

        UI.getCurrent()?.access {
            Notifications.showError(throwable.message ?: "Unexpected error occurred")
        }
    }
}