package com.testtask.common.utils

import org.springframework.data.jpa.domain.Specification

object SpecificationUtils {
    fun <T> containsIgnoreCase(
        fieldName: String,
        value: String?
    ): Specification<T>? {
        val normalized = value?.trim()?.takeIf(String::isNotBlank)?.lowercase()
            ?: return null

        return Specification { root, _, cb ->
            cb.like(
                cb.lower(root.get(fieldName)),
                "%$normalized%"
            )
        }
    }
}