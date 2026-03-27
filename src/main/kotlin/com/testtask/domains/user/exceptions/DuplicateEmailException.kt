package com.testtask.domains.user.exceptions

class DuplicateEmailException(email: String) :
    RuntimeException("User with email '$email' already exists")