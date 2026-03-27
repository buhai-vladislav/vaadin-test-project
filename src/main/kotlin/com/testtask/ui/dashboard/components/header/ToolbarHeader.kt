package com.testtask.ui.dashboard.components.header

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

object ToolbarHeader {

    fun build(isAdmin: Boolean, logout: () -> Unit): HorizontalLayout {
        val title = H1("Dashboard")
        val label = when {
            isAdmin -> "Admin"
            else -> "User (read-only)"
        }
        val roleLabel = Span("Role: $label")

        val left = VerticalLayout(title, roleLabel).apply {
            isPadding = false
            isSpacing = false
        }

        val logoutButton = Button("Logout") {
            logout()
        }

        val right = HorizontalLayout(logoutButton).apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }

        return HorizontalLayout(left, right).apply {
            width = "100%"
            justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }
    }
}