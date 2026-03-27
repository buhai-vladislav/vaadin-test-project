package com.testtask

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

@SpringBootApplication
@EnableMethodSecurity
class TestTaskApplication

fun main(args: Array<String>) {
    runApplication<TestTaskApplication>(*args)
}