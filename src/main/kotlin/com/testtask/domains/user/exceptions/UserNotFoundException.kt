package com.testtask.domains.user.exceptions

class UserNotFoundException(userId: Long) :
    RuntimeException("User with id=$userId was not found")