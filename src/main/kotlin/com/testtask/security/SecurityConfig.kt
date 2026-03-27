package com.testtask.security

import com.testtask.ui.login.LoginView
import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
class SecurityConfig : VaadinWebSecurity() {

    override fun configure(http: HttpSecurity) {
        super.configure(http)
        setLoginView(http, LoginView::class.java)
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val admin = User.withUsername("admin")
            .password("{noop}admin123")
            .roles("ADMIN")
            .build()

        val user = User.withUsername("user")
            .password("{noop}user123")
            .roles("USER")
            .build()

        return InMemoryUserDetailsManager(admin, user)
    }
}