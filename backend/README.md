# Java Recipe Backend

This is the backend service for the Java Recipe application, built with Spring Boot.

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Gradle (or use the included Gradle wrapper)

## Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd java-recipe/backend
   ```

2. **Configure MySQL**
   - Make sure MySQL is installed and running
   - The application is configured to create a database named `javarecipe` if it doesn't exist
   - Update the database credentials in `src/main/resources/application.properties` if needed:
     ```properties
     spring.datasource.username=root
     spring.datasource.password=
     ```

3. **Build the application**
   ```bash
   ./gradlew build
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

The application will start on port 8080. You can test it by accessing:
- http://localhost:8080/api/test

## Project Structure

The project follows a modular monolith architecture with the following modules:

- `user`: User management and authentication
- `recipe`: Recipe management
- `comment`: Comment functionality
- `interaction`: User interactions (likes, favorites, reviews)
- `notification`: Notification system
- `common`: Shared utilities and configurations

## Development

- The database schema is automatically created/updated using JPA/Hibernate
- During development, the `spring.jpa.hibernate.ddl-auto=update` setting ensures the database schema stays in sync with entity classes 