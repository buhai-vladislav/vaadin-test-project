package com.testtask.security

import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    fun hasRole(role: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication ?: return false

        return authentication.authorities.any { it.authority == "ROLE_$role" }
    }

    fun isAdmin(): Boolean = hasRole("ADMIN")

    fun username(): String? {
        return SecurityContextHolder.getContext().authentication?.name
    }
}