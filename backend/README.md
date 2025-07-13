# Java Recipe Backend

This is the backend service for the Java Recipe application, built with Spring Boot. A comprehensive recipe sharing platform with user interactions, notifications, and social features.

## 🚀 Current Status

**Production Ready** - Core functionality fully implemented and tested:
- ✅ User Management & Authentication (JWT-based with password reset)
- ✅ Recipe Management (CRUD with images, ingredients, instructions)
- ✅ Image Management (Cloudinary integration for recipes and avatars)
- ✅ Avatar Management (upload, delete, change with optimization)
- ✅ Social Features (Comments, Likes, Favorites, Reviews)
- ✅ Smart Notification System with Batching
- ✅ Admin Management System (categories, warnings, users)
- ✅ User Search & Filtering (advanced admin capabilities)
- ✅ Role Management (promote/demote users)
- ✅ "Recipes I Can Make" Search (ingredient-based recipe discovery)
- ✅ Comprehensive REST API with Pagination
- ✅ Full Test Coverage (50+ endpoints tested)

## 🚀 Quick Start Guide

### Prerequisites

Before you begin, ensure you have the following installed:
- **Java 17 or higher** ([Download here](https://adoptium.net/))
- **MySQL 8.0 or higher** ([Download here](https://dev.mysql.com/downloads/mysql/))
- **Git** ([Download here](https://git-scm.com/downloads))

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/java-recipe.git
cd java-recipe/backend
```

### 2. Database Setup

Create a MySQL database for the application:

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database
CREATE DATABASE java_recipe_db;

-- Create user (optional, for security)
CREATE USER 'recipe_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON java_recipe_db.* TO 'recipe_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure Application Properties

Create or update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/java_recipe_db
spring.datasource.username=recipe_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Configuration
jwt.secret=your-super-secret-jwt-key-here-make-it-long-and-secure
jwt.expiration=86400000

# Cloudinary Configuration (for image uploads)
cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret

# Server Configuration
server.port=8080
```

**⚠️ Important Security Notes:**
- Replace `your_password` with a secure database password
- Replace JWT secret with a long, random string (at least 32 characters)
- Get your own Cloudinary credentials from [cloudinary.com](https://cloudinary.com/)

### 4. Run the Application

Using Gradle wrapper (recommended):
```bash
# Make the wrapper executable (Linux/Mac)
chmod +x gradlew

# Run the application
./gradlew bootRun
```

On Windows:
```cmd
gradlew.bat bootRun
```

Or using your IDE:
- Import the project as a Gradle project
- Run the `BackendApplication.java` main class

### 5. Verify Installation

The application should start on `http://localhost:8080`. You can verify it's working by:

```bash
# Check if the server is running
curl http://localhost:8080/api/consumer-warnings

# Should return a list of consumer warnings like:
# [{"id":1,"name":"Contains Nuts","description":null,"recipeCount":0}, ...]
```

### 6. Create Your First Admin User

The application automatically creates default data on first startup. You can:

1. **Use the default admin account:**
   - Username: `admin`
   - Email: `admin@javarecipe.com`
   - Password: `admin123`

2. **Or register a new user and promote to admin:**
   ```bash
   # Register a new user
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "yourusername",
       "email": "your@email.com",
       "password": "yourpassword",
       "firstName": "Your",
       "lastName": "Name"
     }'
   ```

3. **Login to get JWT token:**
   ```bash
   # Login with admin credentials
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "admin",
       "password": "admin123"
     }'

   # Save the returned token for authenticated requests
   export JWT_TOKEN="your-jwt-token-here"
   ```

### Additional Development Tools

**IDE Setup:**
- **IntelliJ IDEA**: Import as Gradle project, enable annotation processing
- **VS Code**: Install Java Extension Pack and Spring Boot Extension Pack
- **Eclipse**: Import as existing Gradle project

**Useful Gradle Commands:**
```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Clean build
./gradlew clean build

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Generate test reports
./gradlew test jacocoTestReport
```

**Database Management:**
```bash
# Reset database (drops and recreates all tables)
# Set spring.jpa.hibernate.ddl-auto=create-drop in application.properties

# View database schema
mysql -u recipe_user -p java_recipe_db
SHOW TABLES;
DESCRIBE recipes;
```

## 🧪 Testing

Run the test suite:
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "*RecipeServiceTest*"

# Run tests with coverage
./gradlew test jacocoTestReport
```

Test coverage reports are generated in `build/reports/jacoco/test/html/index.html`

## 🚀 Production Deployment

### Environment Variables

For production, use environment variables instead of application.properties:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/java_recipe_db
export SPRING_DATASOURCE_USERNAME=your-db-user
export SPRING_DATASOURCE_PASSWORD=your-db-password
export JWT_SECRET=your-production-jwt-secret
export CLOUDINARY_CLOUD_NAME=your-cloud-name
export CLOUDINARY_API_KEY=your-api-key
export CLOUDINARY_API_SECRET=your-api-secret
```

### Docker Deployment

Create a `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
./gradlew build
docker build -t java-recipe-backend .
docker run -p 8080:8080 java-recipe-backend
```

## 📁 Project Structure

```
backend/
├── src/main/java/com/javarecipe/backend/
│   ├── admin/                 # Admin management (categories, warnings, users)
│   ├── auth/                  # Authentication & authorization
│   ├── comment/               # Comment system
│   ├── common/                # Shared utilities & configuration
│   ├── image/                 # Image management (Cloudinary)
│   ├── interaction/           # Social features (likes, favorites, reviews)
│   ├── notification/          # Notification system with batching
│   ├── recipe/                # Recipe management & search
│   ├── user/                  # User management & profiles
│   └── BackendApplication.java # Main application class
├── src/main/resources/
│   ├── application.properties # Configuration file
│   └── data.sql              # Initial data (categories, warnings, admin user)
├── src/test/java/            # Unit and integration tests
├── build.gradle              # Gradle build configuration
└── README.md                 # This file
```

### Key Components

- **Controllers**: REST API endpoints (`@RestController`)
- **Services**: Business logic (`@Service`)
- **Repositories**: Data access layer (`@Repository`)
- **Entities**: JPA database models (`@Entity`)
- **DTOs**: Data transfer objects for API responses
- **Config**: Security, CORS, and application configuration

## � API Documentation

The application provides comprehensive REST APIs with **50+ endpoints** covering all functionality.

**📖 For Complete API Documentation**: Please refer to [`api.txt`](api.txt)

The `api.txt` file contains:
- ✅ **All 50+ endpoint specifications** with detailed request/response examples
- ✅ **Authentication and authorization** details for each endpoint
- ✅ **Request body schemas** and response formats
- ✅ **Query parameters** and pagination options
- ✅ **HTTP status codes** and error handling
- ✅ **Practical testing examples** with curl and HTTPie
- ✅ **Step-by-step testing workflows** for all features
- ✅ **Admin management examples** and user operations

**🔗 Base URL**: `http://localhost:8080/api`

**🔑 Authentication**: JWT-based with role-based authorization (USER, ADMIN)

**� Quick Test**: After starting the app, test connectivity with:
```bash
curl http://localhost:8080/api/consumer-warnings
```

## 🏗️ Architecture & Features

### Core Systems

**User Management**
- JWT-based authentication and authorization
- User registration, login, and profile management
- Password reset via email integration
- Manual password change (no constraints, current password verification)
- Avatar management (upload, delete, change)
- Role-based access control (USER, ADMIN)

**Recipe Management**
- Full CRUD operations for recipes
- Multi-image support per recipe with Cloudinary integration
- Direct image upload with automatic optimization (800x600, quality auto)
- Ingredients and step-by-step instructions
- Categories and consumer warnings
- View count tracking for popularity metrics

**Image Management**
- **Cloudinary Integration**: Professional cloud image storage and delivery
- **Recipe Images**: Direct multipart upload with automatic resizing and optimization
- **Avatar Management**: 200x200 face-focused cropping with smart replacement
- **Image Processing**: Automatic format conversion (WebP/AVIF), compression, and quality optimization
- **File Validation**: Type checking, size limits (10MB), and content validation
- **Smart Cleanup**: Automatic deletion of old images when uploading new ones

**Social Features**
- **Comments & Replies**: Nested comment system on recipes
- **Likes**: Toggle like/unlike for recipes and comments
- **Favorites**: Save recipes with paginated browsing
- **Reviews**: 1-5 star ratings with text reviews and aggregated statistics

**Smart Notification System**
- 5 notification types: recipe likes, comments, replies, comment likes, reviews
- **Intelligent Batching**: Prevents spam by batching like notifications (1-hour window)
  - Single like: "alice liked your recipe 'Pasta'"
  - Multiple likes: "alice and 2 others liked your recipe 'Pasta'"
- Real-time unread counts and bulk mark-as-read functionality
- Self-notification prevention

**Admin Management System**
- **Category Management**: Full CRUD operations for recipe categories with usage tracking
- **Consumer Warning Management**: Complete management of allergen/dietary warnings
- **User Management**: Comprehensive admin control over all users with activity metrics
- **User Search & Filtering**: Advanced search by username, email, name, status, and role
- **Role Management**: Promote/demote users between USER and ADMIN roles
- **Activity Tracking**: Real-time metrics for recipes, comments, reviews, likes, and favorites
- **Safe Operations**: Protected deletion with content validation
- **Admin-Only Access**: Proper role-based authorization for all admin endpoints

**"Recipes I Can Make" Search System**
- **Ingredient Discovery**: Extract and search all available ingredients from recipes
- **Smart Recipe Matching**: Sophisticated algorithm with percentage-based matching
- **Match Calculation**: Accurate tracking of available vs missing ingredients
- **Flexible Filtering**: Filter by match percentage, exact matches, and categories
- **Multiple Sorting**: Sort by match percentage, rating, popularity, or date
- **Missing Ingredient Tracking**: Shows exactly what ingredients are needed
- **Case-Insensitive Search**: Flexible ingredient name matching and comparison
- **Recipe Count Tracking**: Shows how many recipes use each ingredient

### Technical Highlights

**Database Design**
- Modular entity relationships with proper constraints
- Automatic schema generation and updates via JPA/Hibernate
- Optimized queries with pagination support

**API Design**
- RESTful endpoints with consistent response formats
- Comprehensive error handling and validation
- Pagination for all list operations
- Authorization checks on all protected endpoints

**Testing**
- Unit tests for all major functionality
- Service layer testing with mocking
- Repository testing for data operations

## 📁 Project Structure

The project follows a modular monolith architecture with the following modules:

```
src/main/java/com/javarecipe/backend/
├── user/           # User management, authentication, and avatars
├── recipe/         # Recipe CRUD operations with image upload
├── comment/        # Comment and reply functionality
├── interaction/    # Likes, favorites, and reviews
├── notification/   # Smart notification system
├── admin/          # Admin management system (categories, warnings, users)
├── common/         # Shared utilities, configurations, and Cloudinary service
└── config/         # Security and application configuration
```

## 🧪 Testing

Run the test suite:
```bash
./gradlew test
```

Run specific test classes:
```bash
./gradlew test --tests "*NotificationServiceTest*"
./gradlew test --tests "*LikeServiceTest*"
./gradlew test --tests "*AvatarManagementTest*"
```



## 📸 Image Upload Examples

### Upload Avatar
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@profile-picture.jpg" \
  http://localhost:8080/api/users/me/avatar
```

### Upload Recipe Image
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@recipe-image.jpg" \
  http://localhost:8080/api/images/upload/recipe
```

### Create Recipe with Images
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "title=Chocolate Cake" \
  -F "description=Delicious chocolate cake" \
  -F "isPublished=true" \
  -F "images=@image1.jpg" \
  -F "images=@image2.jpg" \
  http://localhost:8080/api/recipes/with-images
```





## �🚧 Development

- The database schema is automatically created/updated using JPA/Hibernate
- During development, the `spring.jpa.hibernate.ddl-auto=update` setting ensures the database schema stays in sync with entity classes
- All endpoints require authentication except registration, login, and password reset
- Use tools like Postman or curl to test the API endpoints

## 🔮 Upcoming Features

- Content moderation & reporting system
- Real-time notifications via WebSocket
- Email notification summaries

## 🌟 Recent Updates

**"Recipes I Can Make" Search System** - Intelligent ingredient-based recipe discovery:
- Smart ingredient extraction and search from all recipes
- Sophisticated recipe matching algorithm with percentage calculations
- Flexible filtering by match percentage, exact matches, and categories
- Missing ingredient tracking and shopping assistance
- Multiple sorting options for optimal recipe discovery

**Admin Management System** - Complete administrative control over the platform:
- Category management with CRUD operations and usage tracking
- Consumer warning management for allergens and dietary restrictions
- Comprehensive user management with activity metrics and role control
- Advanced user search and filtering capabilities
- Safe operations with content validation and proper authorization

**Image Management System** - Complete Cloudinary integration for professional image handling:
- Direct image upload for recipes and avatars
- Automatic optimization (resizing, compression, format conversion)
- Smart image replacement and cleanup
- Face-focused avatar cropping
- Global CDN delivery for fast image loading

**Enhanced User Management** - Improved user experience:
- Manual password change with flexible constraints
- Avatar upload, change, and delete functionality
- Streamlined user profile management
- User warning assignment for recipes

## 🔧 Troubleshooting

### Common Issues

**1. Database Connection Failed**
```
Error: Could not connect to MySQL server
```
**Solution:**
- Ensure MySQL is running: `sudo systemctl start mysql` (Linux) or start MySQL service (Windows)
- Check database credentials in `application.properties`
- Verify database exists: `SHOW DATABASES;` in MySQL

**2. Port Already in Use**
```
Error: Port 8080 is already in use
```
**Solution:**
- Kill process using port 8080: `lsof -ti:8080 | xargs kill -9` (Mac/Linux)
- Or change port in `application.properties`: `server.port=8081`

**3. JWT Token Invalid**
```
Error: 401 Unauthorized
```
**Solution:**
- Check JWT secret configuration
- Ensure token is included in Authorization header: `Bearer <token>`
- Verify token hasn't expired (default: 24 hours)

**4. Cloudinary Upload Failed**
```
Error: Image upload failed
```
**Solution:**
- Verify Cloudinary credentials in `application.properties`
- Check internet connection
- Ensure image file size is under 10MB

**5. Build Failed**
```
Error: Could not resolve dependencies
```
**Solution:**
- Check internet connection
- Clear Gradle cache: `./gradlew clean`
- Refresh dependencies: `./gradlew build --refresh-dependencies`

### Logs and Debugging

**Enable Debug Logging:**
Add to `application.properties`:
```properties
logging.level.com.javarecipe=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

**View Application Logs:**
```bash
# Real-time logs
./gradlew bootRun

# Or check log files (if configured)
tail -f logs/application.log
```

## 🤝 Contributing

We welcome contributions! Here's how to get started:

### Development Workflow

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
4. **Run tests**
   ```bash
   ./gradlew test
   ```
5. **Commit your changes**
   ```bash
   git commit -m "Add: your feature description"
   ```
6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```
7. **Create a Pull Request**

### Code Style Guidelines

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Write unit tests for new functionality
- Keep methods small and focused (max 20 lines)

### Testing Requirements

- All new features must include unit tests
- Maintain test coverage above 80%
- Integration tests for API endpoints
- Mock external dependencies (Cloudinary, etc.)

### Commit Message Format

```
Type: Brief description

Detailed explanation if needed

- Add specific changes
- Fix specific issues
- Update specific components
```

Types: `Add`, `Fix`, `Update`, `Remove`, `Refactor`, `Test`, `Docs`

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📖 Additional Documentation

- **Complete API Reference**: See `api.txt` file for comprehensive API documentation with detailed request/response examples
- **Database Schema**: Auto-generated via JPA/Hibernate - check entity classes for structure
- **Security Configuration**: JWT-based authentication with role-based authorization
- **Image Management**: Cloudinary integration with automatic optimization and CDN delivery

## 🆘 Support

- **Documentation**: Check this README and the `api.txt` file for comprehensive API documentation
- **Issues**: Report bugs and request features via GitHub Issues
- **Discussions**: Join discussions in GitHub Discussions
- **Testing**: All 50+ endpoints have been thoroughly tested and verified working
- **Email**: Contact the maintainers for additional support

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Cloudinary for image management services
- MySQL for reliable database services
- All contributors who helped build this project

---

**Happy Coding! 🚀**

Built with ❤️ using Spring Boot, MySQL, and modern Java practices.