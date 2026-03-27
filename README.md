# Test Task Vaadin App

A small full-stack Kotlin application built with Spring Boot, Vaadin, PostgreSQL, Liquibase, and Docker Compose.

## Tech Stack

- Kotlin
- Spring Boot
- Vaadin
- PostgreSQL
- Liquibase
- Docker Compose

## Features

- Login page
- Two roles: User and Admin
- Dashboard with user list
- Search by name and email
- Sorting by name, email, created date, updated date
- Admin CRUD for users
- User action logging
- Automatic database schema migration
- Automatic seeding with 500 test users

## Default Credentials

### Admin
- Username: `admin`
- Password: `admin123`

### User
- Username: `user`
- Password: `user123`

## How to Run

Run the whole application with:

```bash
make up