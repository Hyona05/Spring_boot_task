# REST Task

Spring Boot based REST API application for managing Trainers, Trainees and Trainings.

---

## Technologies

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- H2 Database
- Maven
- Lombok
- Jakarta Validation
- OpenAPI / Swagger
- JUnit 5
- Mockito
- BCrypt Password Encoder

---

## REST API Features

### Authentication

- Login
- Change Password

### Trainee Management

- Register Trainee
- Get Trainee Profile
- Update Trainee Profile
- Delete Trainee Profile
- Activate/Deactivate Trainee
- Get Trainee Trainings
- Update Trainee Trainer List
- Get Not Assigned Active Trainers

### Trainer Management

- Register Trainer
- Get Trainer Profile
- Update Trainer Profile
- Activate/Deactivate Trainer
- Get Trainer Trainings

### Training Management

- Add Training
- Get Training Types

---

## Database Schema

### Tables

- users
- trainers
- trainees
- trainings
- training_types
- trainee_trainer

### Relationships

| Entity | Relationship |
|----------|-------------|
| User → Trainer | One-to-One |
| User → Trainee | One-to-One |
| Trainee ↔ Trainer | Many-to-Many |
| Training → Trainee | Many-to-One |
| Training → Trainer | Many-to-One |
| Training → TrainingType | Many-to-One |

---

## Business Rules

- Username and password are generated automatically during registration.
- User cannot be registered as both Trainer and Trainee.
- Passwords are encrypted using BCrypt.
- All endpoints except registration require authentication.
- Username cannot be changed.
- Training Types are predefined and read-only.
- Trainee deletion performs hard delete with cascade training deletion.
- DTOs are used for request and response mapping.
- Validation is implemented for all required fields.

---

## Logging

Implemented according to task requirements:

### Transaction Logging

Each request receives a unique transactionId.

### REST Logging

Logged information:

- Endpoint
- HTTP Method
- Response Status
- Error Information

Sensitive information such as passwords is never logged.

---

## Error Handling

Global exception handling is implemented using `@RestControllerAdvice`.

Handled exceptions:

- AuthenticationException
- ResourceNotFoundException
- ValidationException
- MethodArgumentNotValidException
- RuntimeException

---

## API Documentation

Swagger UI:

http://localhost:8080/swagger-ui/index.html

---

## H2 Console

http://localhost:8080/h2-console

JDBC URL:
jdbc:h2:mem:rest_task

Username:
sa

Password:
root123

---

## Build

mvn clean install

## Run

mvn spring-boot:run

## Test

mvn test

---

## Architecture

Controller Layer
↓
Service Layer
↓
Repository Layer
↓
Database Layer

The project follows REST principles, layered architecture, SOLID, DRY and KISS principles.