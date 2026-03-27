package com.testtask.ui.login

import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
class LoginView : VerticalLayout(), BeforeEnterObserver {
    private val loginForm = LoginForm()

    init {
        setSizeFull()
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        alignItems = FlexComponent.Alignment.CENTER

        loginForm.action = "login"

        add(loginForm)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        val hasError = event.location.queryParameters.parameters.containsKey("error")
        loginForm.isError = hasError
    }
}