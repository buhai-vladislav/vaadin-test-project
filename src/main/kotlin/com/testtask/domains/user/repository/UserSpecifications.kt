package com.testtask.domains.user.repository

import com.testtask.common.utils.SpecificationUtils.containsIgnoreCase
import com.testtask.domains.user.dto.UserFilter
import com.testtask.domains.user.entity.UserEntity
import org.springframework.data.jpa.domain.Specification

object UserSpecifications {
    fun withFilter(filter: UserFilter): Specification<UserEntity> {
        val specifications: List<Specification<UserEntity>> = listOfNotNull(
            containsIgnoreCase("name", filter.name),
            containsIgnoreCase("email", filter.email)
        )

        return specifications
            .reduceOrNull(Specification<UserEntity>::and)
            ?: Specification.where(null)
    }
}