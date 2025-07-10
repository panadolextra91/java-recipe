# Java Recipe Backend

This is the backend service for the Java Recipe application, built with Spring Boot. A comprehensive recipe sharing platform with user interactions, notifications, and social features.

## 🚀 Current Status

**Production Ready** - Core functionality fully implemented and tested:
- ✅ User Management & Authentication (including manual password change)
- ✅ Recipe Management (CRUD with images, ingredients, instructions)
- ✅ Image Management (Cloudinary integration for recipes and avatars)
- ✅ Avatar Management (upload, delete, change with optimization)
- ✅ Social Features (Comments, Likes, Favorites, Reviews)
- ✅ Smart Notification System with Batching
- ✅ Admin Management System (categories, warnings, users)
- ✅ User Search & Filtering (advanced admin capabilities)
- ✅ Role Management (promote/demote users)
- ✅ Comprehensive API with Pagination
- ✅ Full Test Coverage

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

3. **Configure Cloudinary (for image upload)**
   - The application is pre-configured with Cloudinary credentials
   - Images are automatically uploaded to the configured Cloudinary account
   - Configuration in `application.properties`:
     ```properties
     cloudinary.cloud-name=duy8dombh
     cloudinary.api-key=352188284543682
     cloudinary.api-secret=dcED4bZAlKhDr1trgPze_9Z1DK8
     ```

4. **Build the application**
   ```bash
   ./gradlew build
   ```

5. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

The application will start on port 8080. You can test it by accessing:
- http://localhost:8080/api/test

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

## 🔌 API Endpoints

### Authentication & User Management
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/forgot-password` - Password reset request
- `POST /api/auth/reset-password` - Password reset confirmation
- `POST /api/users/me/change-password-simple` - Manual password change
- `POST /api/users/me/avatar` - Upload/change avatar
- `DELETE /api/users/me/avatar` - Delete avatar

### Recipes & Images
- `GET /api/recipes` - List recipes (paginated)
- `POST /api/recipes` - Create recipe (JSON)
- `POST /api/recipes/with-images` - Create recipe with direct image upload
- `GET /api/recipes/{id}` - Get recipe details
- `PUT /api/recipes/{id}` - Update recipe
- `DELETE /api/recipes/{id}` - Delete recipe
- `POST /api/images/upload/recipe` - Upload recipe image
- `POST /api/images/upload/{folder}` - Upload image to specific folder
- `DELETE /api/images/{publicId}` - Delete image from Cloudinary
- `GET /api/images/{publicId}/thumbnail` - Generate thumbnail URL

### Social Features
- `POST /api/comments` - Create comment
- `POST /api/likes/recipe/{id}` - Toggle recipe like
- `POST /api/likes/comment/{id}` - Toggle comment like
- `POST /api/favorites/recipe/{id}` - Toggle favorite
- `GET /api/favorites` - Get user's favorites
- `POST /api/reviews` - Create review

### Notifications
- `GET /api/notifications` - Get notifications (paginated)
- `GET /api/notifications/unread-count` - Get unread count
- `PUT /api/notifications/{id}/read` - Mark as read
- `PUT /api/notifications/mark-all-read` - Mark all as read

### Consumer Warnings (Public)
- `GET /api/consumer-warnings` - Get all consumer warnings
- `GET /api/consumer-warnings/{id}` - Get consumer warning by ID

### Admin Management (Admin Only)
- `GET /api/admin/categories` - Get categories (paginated)
- `POST /api/admin/categories` - Create category
- `PUT /api/admin/categories/{id}` - Update category
- `DELETE /api/admin/categories/{id}` - Delete category
- `GET /api/admin/consumer-warnings` - Get warnings (paginated)
- `POST /api/admin/consumer-warnings` - Create warning
- `PUT /api/admin/consumer-warnings/{id}` - Update warning
- `DELETE /api/admin/consumer-warnings/{id}` - Delete warning
- `GET /api/admin/users` - Get users (paginated)
- `GET /api/admin/users/search` - Search users
- `GET /api/admin/users/status/{active}` - Filter by status
- `GET /api/admin/users/role/{role}` - Filter by role
- `PUT /api/admin/users/{id}/status` - Update user status
- `PUT /api/admin/users/{id}/roles` - Update user roles
- `DELETE /api/admin/users/{id}` - Delete user
- `GET /api/admin/users/{id}/activity` - Get user activity stats

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

## �️ Admin Management Examples

### Create Category
```bash
curl -X POST \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Mediterranean", "description": "Mediterranean cuisine recipes"}' \
  http://localhost:8080/api/admin/categories
```

### Create Consumer Warning
```bash
curl -X POST \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Contains Sesame", "description": "This product contains sesame seeds"}' \
  http://localhost:8080/api/admin/consumer-warnings
```

### Search Users
```bash
curl -X GET \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  "http://localhost:8080/api/admin/users/search?query=john&page=0&size=10"
```

### Promote User to Admin
```bash
curl -X PUT \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"roles": ["ROLE_USER", "ROLE_ADMIN"]}' \
  http://localhost:8080/api/admin/users/1/roles
```

### Deactivate User
```bash
curl -X PUT \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"active": false}' \
  http://localhost:8080/api/admin/users/1/status
```

## �🚧 Development

- The database schema is automatically created/updated using JPA/Hibernate
- During development, the `spring.jpa.hibernate.ddl-auto=update` setting ensures the database schema stays in sync with entity classes
- All endpoints require authentication except registration, login, and password reset
- Use tools like Postman or curl to test the API endpoints

## 🔮 Upcoming Features

- Content moderation & reporting system
- Advanced search functionality
- "Recipes I Can Make" ingredient-based search
- Real-time notifications via WebSocket
- Email notification summaries

## 🌟 Recent Updates

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