# Lost and Found Application

A modern JavaFX desktop application for managing lost and found items. Users can post lost or found items, search for matches, connect with other users, and claim items through an intuitive graphical interface.

## Features

- **User Management**: User registration, login, and role-based access control (User, Moderator, Admin)
- **Item Management**: Post lost/found items with descriptions, categories, locations, and images
- **Search & Discovery**: Advanced search functionality to find matching items
- **Messaging System**: Direct messaging between users to discuss item details
- **Item Claims**: Claim lost/found items with verification
- **Admin Dashboard**: Manage users and monitor platform activity
- **Activity Logging**: Track user actions and system events
- **SQLite Database**: Local embedded database for data persistence
- **Image Uploads**: Support for item images stored locally

## Prerequisites

Before running this project, ensure you have the following installed:

- **Java 24 or higher** - Required for Java module system and latest JavaFX compatibility
- **Apache Maven 3.6+** - Project uses Maven for dependency management and building
  - Optional: Use the included Maven wrapper (`./mvnw` on Unix/Mac, `mvnw.cmd` on Windows)
- **SQLite 3.x** - JDBC driver included in dependencies
- **Operating System**: Windows, macOS, or Linux (tested on macOS with Java 24)
- **System Resources**:
  - Minimum 500MB free disk space
  - 2GB RAM recommended
  - Display capable of 1024x768 resolution minimum

## Quick Start

### 1. Navigate to Project Directory

```bash
cd /Users/fc/Documents/AAASEM/Programming/GroupProject/Lost_and_Found
```

### 2. Initialize the Database (First Time Only)

The database must be initialized before the application starts. This creates all required tables and adds a default admin user.

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="net.javaguids.lost_and_found.database.DatabaseInitializer"

# Or compile and run directly (if build is already complete)
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) \
  net.javaguids.lost_and_found.database.DatabaseInitializer
```

**What this does:**
- Creates SQLite database file (`lostandfound.db`)
- Creates necessary tables: `users`, `items`, `messages`, `activity_logs`
- Creates default admin account:
  - **Username**: `admin`
  - **Password**: `admin123`
  - **Email**: `admin@lostandfound.com`

### 3. Build the Project

```bash
# Clean previous builds and compile
mvn clean compile

# Or use Maven wrapper (Unix/Mac)
./mvnw clean compile

# Or use Maven wrapper (Windows)
mvnw.cmd clean compile
```

### 4. Run the Application

```bash
# Run with JavaFX plugin
mvn javafx:run

# Or with Maven wrapper
./mvnw javafx:run
```

**First Login:**
- Username: `admin`
- Password: `admin123`

**Expected Behavior:**
- Application window opens with login screen (400x400px)
- After login, user is directed to appropriate dashboard (Admin/Moderator/User)
- Database file `lostandfound.db` appears in project root
- Images folder `uploads/images/` is created for storing item photos

## Building the Project

### Maven Commands

#### Clean Compilation
```bash
mvn clean compile
```
- Removes all previously built files
- Recompiles all source code
- Use this before first build or after major changes

#### Run Tests
```bash
mvn test
```
- Executes all JUnit 5 tests
- Tests include ItemService, MessageService, validation tests
- Requires JavaFX module arguments configured (see [Troubleshooting](#testing-issues))

#### Package Application
```bash
mvn clean package
```
- Creates JAR file in `target/` directory
- Includes all dependencies

#### Run Application
```bash
mvn javafx:run
```
- Compiles and launches the application
- Most common development command

### Using Maven Wrapper

If Maven is not installed globally, use the included Maven wrapper:

```bash
# Unix/Mac
./mvnw clean compile
./mvnw javafx:run

# Windows
mvnw.cmd clean compile
mvnw.cmd javafx:run
```

## Running the Application

### Via Maven (Recommended for Development)
```bash
mvn javafx:run
```

### Via Compiled JAR (After Packaging)
```bash
# Build first
mvn clean package

# Run the JAR (requires proper Java module path setup)
java -p target/classes:~/.m2/repository/org/openjfx/javafx-base/21.0.6/javafx-base-21.0.6-mac.jar \
     --add-modules javafx.controls,javafx.fxml,javafx.web \
     -m net.javaguids.lost_and_found/net.javaguids.lost_and_found.Launcher
```

### Application Flow

1. **Startup**: Shows login screen
2. **Login**: Enter credentials (default: admin/admin123)
3. **Dashboard**: Based on user role:
   - **Admin**: Full system access, user management
   - **Moderator**: Item moderation, user monitoring
   - **User**: Post items, search, messaging
4. **Navigation**: Use application buttons to move between views
5. **Database**: All changes automatically saved to `lostandfound.db`

## Database Initialization

### Why It's Needed

The SQLite database must exist and be initialized with the proper schema before the application can run. This is a one-time setup process.

### Database Location

- **File**: `lostandfound.db`
- **Location**: Project root directory (`/Users/fc/Documents/AAASEM/Programming/GroupProject/Lost_and_Found/`)
- **Size**: Grows as data is added (starts empty, ~96KB with sample data)

### Database Tables

#### `users` Table
Stores user account information and authentication:
- `user_id` (TEXT PRIMARY KEY): Unique user identifier
- `username` (TEXT UNIQUE): Login username
- `email` (TEXT UNIQUE): User email address
- `password_hash` (TEXT): Bcrypt-hashed password
- `role` (TEXT): User role - `admin`, `moderator`, or `user`
- `created_at` (TEXT): Account creation timestamp

#### `items` Table
Stores lost/found item listings:
- `item_id` (TEXT PRIMARY KEY): Unique item identifier
- `title` (TEXT): Item name/title
- `description` (TEXT): Detailed item description
- `category` (TEXT): Item category (e.g., Electronics, Clothing, Documents)
- `location` (TEXT): Location where item was lost/found
- `date_posted` (TEXT): When the listing was created
- `date_lost_found` (TEXT): When the item was actually lost/found
- `status` (TEXT): Listing status - `active`, `claimed`, `resolved`
- `type` (TEXT): `lost` or `found`
- `posted_by_user_id` (TEXT FOREIGN KEY): ID of user who posted
- `image_path` (TEXT): Path to item image
- `reward` (REAL): Reward amount if applicable (lost items)

#### `messages` Table
Stores user-to-user messages:
- `message_id` (TEXT PRIMARY KEY): Unique message identifier
- `sender_id` (TEXT FOREIGN KEY): ID of message sender
- `receiver_id` (TEXT FOREIGN KEY): ID of message recipient
- `content` (TEXT): Message body
- `timestamp` (TEXT): When message was sent

#### `activity_logs` Table
Tracks user actions for auditing:
- `log_id` (TEXT PRIMARY KEY): Unique log entry identifier
- `user_id` (TEXT FOREIGN KEY): ID of user performing action
- `action` (TEXT): Type of action (e.g., LOGIN, POST_ITEM, CLAIM_ITEM)
- `details` (TEXT): Additional action details
- `timestamp` (TEXT): When action occurred

### Initialization Process

The `DatabaseInitializer` class handles initialization:

```java
// Located at: src/main/java/net/javaguids/lost_and_found/database/DatabaseInitializer.java
public class DatabaseInitializer {
    public static void main(String[] args) {
        // Creates tables if they don't exist
        // Creates default admin user
        // Sets up foreign key constraints
    }
}
```

### Default Admin User

After initialization, the following account is created:
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@lostandfound.com`
- **Role**: `admin`

Change this password after first login for security.

## Project Structure

```
Lost_and_Found/
├── src/
│   ├── main/
│   │   ├── java/net/javaguids/lost_and_found/
│   │   │   ├── Launcher.java                    # Application entry point
│   │   │   ├── HelloApplication.java            # JavaFX Application class
│   │   │   ├── controllers/                     # View controllers (12 files)
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   ├── UserDashboardController.java
│   │   │   │   ├── AdminDashboardController.java
│   │   │   │   ├── SearchController.java
│   │   │   │   ├── ItemDetailsController.java
│   │   │   │   ├── PostItemController.java
│   │   │   │   ├── ClaimItemController.java
│   │   │   │   ├── MessagesController.java
│   │   │   │   ├── CreateUserController.java
│   │   │   │   ├── EditUserController.java
│   │   │   │   └── ModeratorDashboardController.java
│   │   │   ├── database/                        # Database layer
│   │   │   │   ├── DatabaseManager.java         # Connection management
│   │   │   │   ├── DatabaseInitializer.java     # Schema creation
│   │   │   │   ├── repositories/                # Data access objects
│   │   │   │   └── ...
│   │   │   ├── services/                        # Business logic
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── ItemService.java
│   │   │   │   ├── MessageService.java
│   │   │   │   └── ...
│   │   │   ├── model/                           # Data models
│   │   │   │   ├── users/
│   │   │   │   ├── items/
│   │   │   │   ├── enums/
│   │   │   │   └── interfaces/
│   │   │   ├── utils/                           # Utility classes
│   │   │   │   ├── NavigationManager.java       # Scene switching
│   │   │   │   ├── PasswordUtil.java            # Password hashing
│   │   │   │   ├── ValidationUtil.java
│   │   │   │   ├── FileHandler.java             # Image handling
│   │   │   │   ├── AlertUtil.java               # UI alerts
│   │   │   │   └── ...
│   │   │   ├── analytics/                       # Analytics features
│   │   │   ├── messaging/                       # Messaging system
│   │   │   ├── search/                          # Search functionality
│   │   │   ├── context/                         # App context management
│   │   │   ├── exceptions/                      # Custom exceptions
│   │   │   └── module-info.java                 # Java module definition
│   │   └── resources/net/javaguids/lost_and_found/
│   │       ├── login-view.fxml                  # Login UI
│   │       ├── register-view.fxml               # Registration UI
│   │       ├── user-dashboard-view.fxml         # User home
│   │       ├── admin-dashboard-view.fxml        # Admin dashboard
│   │       ├── moderator-dashboard-view.fxml    # Moderator dashboard
│   │       ├── search-view.fxml                 # Item search
│   │       ├── item-details-view.fxml           # Item details
│   │       ├── post-item-view.fxml              # Create listing
│   │       ├── claim-item-view.fxml             # Claim item
│   │       ├── messages-view.fxml               # Messaging UI
│   │       ├── create-user-view.fxml            # Admin user creation
│   │       ├── edit-user-view.fxml              # Admin user editing
│   │       └── hello-view.fxml                  # Welcome screen
│   ├── test/java/net/javaguids/lost_and_found/  # Test suite (31 test files)
│   │   ├── services/
│   │   ├── database/
│   │   ├── utils/
│   │   └── ...
├── uploads/
│   └── images/                                   # User-uploaded item images
├── pom.xml                                       # Maven configuration
├── lostandfound.db                               # SQLite database (created after init)
├── README.md                                     # This file
└── target/                                       # Build output directory
    ├── classes/                                  # Compiled Java classes
    └── ...
```

## Troubleshooting

### Maven Clean and Build Issues

#### Issue: "Failed to execute goal" or Corrupted build state

**Symptoms:**
```
[ERROR] Failed to execute goal ... (various build errors)
[ERROR] Project dependency resolution failure
[ERROR] Compilation errors that don't make sense
```

**Solution:**
```bash
# Always use clean with compilation on first build
mvn clean compile

# This removes the entire target/ directory and rebuilds everything
# Use this when:
# - Strange build errors occur
# - Dependencies seem corrupted
# - After switching Java versions
# - After pulling changes from git
```

**When to Use `mvn clean`:**
- **Before first build**: `mvn clean compile`
- **Before running**: `mvn clean javafx:run`
- **Before packaging**: `mvn clean package`
- **After git pull/merge**: `mvn clean compile`
- **When changing Java version**: `mvn clean compile`
- **If "target" directory is corrupted**: `mvn clean` then rebuild

#### Issue: "BUILD FAILURE: Module not found"

**Symptoms:**
```
[ERROR] Cannot find module net.javaguids.lost_and_found
[ERROR] Class path search issue
```

**Solution:**
```bash
# Ensure clean build with proper compilation
mvn clean compile -U

# The -U flag forces Maven to update dependencies
# This resolves module visibility issues
```

### JavaFX Plugin Issues

#### Issue: "JavaFX modules not found"

**Symptoms:**
```
Exception in thread "main"
java.lang.module.FindException: Module javafx.controls not found
```

**Root Cause:**
- JavaFX not properly configured in pom.xml
- Java module path not set correctly
- Running without Maven (direct java command)

**Solution:**
```bash
# Always use Maven to run JavaFX applications
mvn javafx:run

# DO NOT try to run with java -jar or raw java commands
# The project requires special module configuration via pom.xml

# If still failing, update pom.xml JavaFX dependency:
# Ensure JavaFX version matches Java version (21.0.6 for Java 24)
```

#### Issue: "JVM argument configuration" errors during testing

**Symptoms:**
```
[ERROR] There are test failures.
[ERROR] Access denied to javafx.scene.control
```

**Root Cause:**
- JavaFX modules not properly opened for testing
- Mockito incompatibility with module system

**Solution:**
The `pom.xml` includes special Maven Surefire configuration:
```xml
<configuration>
    <argLine>
        --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED
        --add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
        --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
    </argLine>
</configuration>
```

If tests still fail:
```bash
# Run tests with verbose output
mvn test -X

# Skip tests temporarily to verify build
mvn clean package -DskipTests

# Run specific test class
mvn test -Dtest=ItemServiceTest
```

#### Issue: "No module specification for javafx"

**Symptoms:**
```
Error: Could not find or load main class ...
caused by: java.lang.NoClassDefFoundError: javafx/...
```

**Solution:**
This happens when trying to run without Maven. Use Maven instead:
```bash
# Correct way
mvn javafx:run

# Incorrect (will fail)
java -jar target/Lost_and_Found-1.0-SNAPSHOT.jar  # ❌ Don't do this
java -cp . net.javaguids.lost_and_found.Launcher   # ❌ Don't do this
```

### Database Issues

#### Issue: "Location is not set" error

**Symptoms:**
```
Exception in thread "JavaFX Application Thread"
java.lang.IllegalStateException: Location is not set.
  at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:2556)
  at net.javaguids.lost_and_found.utils.NavigationManager.navigateTo(NavigationManager.java:29)
```

**Root Cause:**
- Missing FXML file that controller is trying to load
- Example: `messages-view.fxml` doesn't exist but code tries to load it

**Solution:**
Check that all referenced FXML files exist in `src/main/resources/net/javaguids/lost_and_found/`:
```bash
ls src/main/resources/net/javaguids/lost_and_found/*.fxml
```

If file is missing, create it or remove the handler that references it.

#### Issue: "ClassNotFoundException" for controllers

**Symptoms:**
```
Caused by: java.lang.ClassNotFoundException:
  net.javaguids.java_final_project.controllers.ItemDetailsController
```

**Root Cause:**
- FXML file has incorrect controller package name
- Package name doesn't match actual controller location
- Wrong project package referenced (old project name)

**Solution:**
Verify FXML controller references match actual package:

**Check file:** `item-details-view.fxml` line 11
```xml
<!-- Should be: -->
fx:controller="net.javaguids.lost_and_found.controllers.ItemDetailsController"

<!-- NOT: -->
fx:controller="net.javaguids.java_final_project.controllers.ItemDetailsController"
```

All FXML files should use: `net.javaguids.lost_and_found.controllers.*`

Run this to check all FXML files:
```bash
grep "fx:controller" src/main/resources/net/javaguids/lost_and_found/*.fxml
```

All lines should reference `net.javaguids.lost_and_found.controllers`, not `java_final_project`.

#### Issue: "SQLite [SQLITE_CANTOPEN]" or "lostandfound.db not found"

**Symptoms:**
```
Error opening database file
java.sql.SQLException: [SQLITE_CANTOPEN] ... (no such file or directory)
```

**Root Cause:**
- Database not initialized before running application
- Database file doesn't exist

**Solution:**
```bash
# Initialize database FIRST before running application
mvn exec:java -Dexec.mainClass="net.javaguids.lost_and_found.database.DatabaseInitializer"

# Verify file was created
ls -la lostandfound.db

# Then run application
mvn javafx:run
```

#### Issue: "No default admin user" or "login fails with admin/admin123"

**Symptoms:**
```
Login page appears but admin/admin123 credentials don't work
Database appears empty
```

**Root Cause:**
- Database initialized but tables not populated with default user
- Database corrupted or incomplete initialization

**Solution:**
```bash
# Remove old database
rm lostandfound.db

# Reinitialize completely
mvn clean compile
mvn exec:java -Dexec.mainClass="net.javaguids.lost_and_found.database.DatabaseInitializer"

# Verify admin user was created
# (Check database with SQLite browser if needed)
sqlite3 lostandfound.db "SELECT username, role FROM users WHERE username='admin';"
```

### FXML Loading Errors

#### Issue: Application launches but views are blank/broken

**Symptoms:**
- Application starts but FXML views don't render
- Buttons don't work
- Text areas empty

**Solution:**
1. Check console for FXML parsing errors
2. Verify all FXML files exist in resources directory
3. Check controller class names in FXML match actual classes:
```bash
# List all FXML files
ls src/main/resources/net/javaguids/lost_and_found/*.fxml

# List all controller classes
ls src/main/java/net/javaguids/lost_and_found/controllers/*.java

# Verify they match by name
```

#### Issue: "NavigationManager.navigateTo() throws exception"

**Symptoms:**
```
Exception when clicking navigation buttons
Navigation between views fails
```

**Solution:**
1. Ensure FXML file exists for requested view
2. Verify filename is exactly correct (case-sensitive on Linux/Mac)
3. Check controller package in FXML is correct:
```bash
# Check specific FXML file
grep "fx:controller" src/main/resources/net/javaguids/lost_and_found/post-item-view.fxml
```

### Runtime Errors

#### Issue: "No suitable GraphicsConfiguration"

**Symptoms:**
```
java.lang.InternalError: Could not find suitable GraphicsConfiguration
```

**Root Cause:**
- Running on headless system (no display)
- Running in Docker or remote environment
- Display server not available

**Solution:**
```bash
# On headless systems, use Monocle (included in test dependencies)
export GLASS_PLATFORM=Monocle
export TESTFX_HEADLESS_MODE=true
mvn test

# For production, ensure X11 forwarding or display is available
```

#### Issue: Module visibility/access errors

**Symptoms:**
```
java.lang.IllegalAccessError: class X cannot access class Y
module X does not "opens" Y to module Z
```

**Root Cause:**
- Module system restrictions (Java 9+ modules)
- package not properly exported in module-info.java

**Solution:**
The `module-info.java` file should have proper configuration. Check:
```bash
# View module configuration
cat src/main/java/module-info.java

# Should include:
# - requires javafx.fxml
# - requires javafx.controls
# - opens net.javaguids.lost_and_found to javafx.fxml
# - exports net.javaguids.lost_and_found.controllers
```

Rebuild if changes made:
```bash
mvn clean compile
```

### Testing Issues

#### Issue: Tests fail with module/JavaFX errors

**Symptoms:**
```
[ERROR] Tests run: X, Failures: Y, Errors: Z
Test failures related to JavaFX or Mockito
```

**Root Cause:**
- JavaFX modules not properly configured for tests
- Mockito can't mock JavaFX classes due to module restrictions

**Solution:**
The `pom.xml` includes Surefire configuration for this. If tests still fail:

```bash
# Run with verbose output to see actual errors
mvn test -e -X

# Skip tests if needed (temporary)
mvn javafx:run -DskipTests

# Run specific test
mvn test -Dtest=ItemServiceTest#testItemCreation
```

#### Issue: TestFX tests not working

**Symptoms:**
```
FxRobotException or interaction with UI fails in tests
```

**Root Cause:**
- TestFX needs JavaFX application initialized
- Graphics environment not available

**Solution:**
```bash
# Ensure tests are configured with TestFX properly
# Run tests with display forwarding if remote:
DISPLAY=:0 mvn test

# Or skip GUI tests:
mvn test -Dtest=*Service* # Only test services, not UI
```

## Development Tips

### IntelliJ IDEA Configuration

1. **Project Setup**
   - File → Project Structure → Project
   - Set SDK to Java 24

2. **Running Application**
   - Run → Run 'javafx:run' (in Maven tool window)
   - Or use Run Configuration with `javafx:run`

3. **Debugging**
   - Set breakpoints in controller classes
   - Run → Debug 'javafx:run'
   - Step through code to understand flow

4. **Building**
   - Maven tool window → Lifecycle → clean → compile
   - Or use Terminal: `mvn clean compile`

### Common Development Workflows

#### Add new view (FXML + Controller)

1. Create new FXML file in `src/main/resources/net/javaguids/lost_and_found/`
2. Create controller class in `src/main/java/net/javaguids/lost_and_found/controllers/`
3. In FXML, set: `fx:controller="net.javaguids.lost_and_found.controllers.YourController"`
4. In controller, add `@FXML` annotations for UI elements
5. Add navigation in `NavigationManager` if needed

#### Modify database schema

1. Edit `DatabaseInitializer.java` to add new table creation SQL
2. Delete `lostandfound.db` file
3. Run: `mvn exec:java -Dexec.mainClass="net.javaguids.lost_and_found.database.DatabaseInitializer"`
4. Update corresponding Repository class

#### Add new service/business logic

1. Create class in `src/main/java/net/javaguids/lost_and_found/services/`
2. Follow singleton pattern if it manages state
3. Call from controllers when needed
4. Write JUnit 5 tests in `src/test/java/net/javaguids/lost_and_found/services/`

### Image Upload Directory

User-uploaded item images are stored in:
```
Lost_and_Found/uploads/images/
```

Directory is created automatically when needed. Images are referenced by path in the `items` table `image_path` column.

### Build Profiles (If Configured)

Currently, no Maven profiles are configured. To add environment-specific builds (dev, test, production), profiles can be added to `pom.xml`.

## Module System Configuration

This project uses Java's module system (Java 9+). Key configuration:

### module-info.java

Located at: `src/main/java/module-info.java`

**Module Name:** `net.javaguids.lost_and_found`

**Key Requirements:**
```java
requires javafx.controls;
requires javafx.fxml;
requires java.sql;
```

**Key Exports:**
```java
exports net.javaguids.lost_and_found.controllers;
exports net.javaguids.lost_and_found.services;
opens net.javaguids.lost_and_found to javafx.fxml;
```

The `opens` directive allows JavaFX FXML loader to access controller classes via reflection.

### Why Modules Matter

- **Type Safety**: Compile-time checking of dependencies
- **Encapsulation**: Only exported packages are visible to other modules
- **Performance**: JVM can optimize module dependencies
- **Security**: Restricted access to internal APIs

If module errors occur:
```bash
# Verify module structure
mvn clean compile -e

# Check module dependencies
java -version
```

## Performance Tips

- **First Launch**: Application may take 2-3 seconds to start (module loading)
- **Image Display**: Large images scale automatically; consider compressing uploads
- **Database**: SQLite works efficiently for this application scale
- **UI**: JavaFX rendering is optimized; smooth performance with modern graphics

## Troubleshooting Summary

| Problem | Command to Try |
|---------|---|
| Build fails mysteriously | `mvn clean compile` |
| Tests error with modules | Check pom.xml Surefire config, then `mvn test -e` |
| Application won't start | Verify database exists: `ls lostandfound.db`, then `mvn javafx:run` |
| FXML file not found | Verify file exists: `ls src/main/resources/.../` |
| Login fails | Reinit database: `mvn exec:java -Dexec.mainClass="...DatabaseInitializer"` |
| JavaFX not found | Use Maven: `mvn javafx:run` (not raw java commands) |
| Image upload fails | Verify `uploads/images/` directory exists |

## Getting Help

For detailed information about specific errors, refer to the sections above or check:

- **JavaFX Issues**: See [JavaFX Plugin Issues](#javafx-plugin-issues) section
- **Database Issues**: See [Database Issues](#database-issues) section
- **Build Issues**: See [Maven Clean and Build Issues](#maven-clean-and-build-issues) section
- **FXML Issues**: See [FXML Loading Errors](#fxml-loading-errors) section

## License

This is a group project for educational purposes.

## Contributors

Developed as part of a group programming project for lost and found item management.

---

**Last Updated**: December 2025
**Java Version**: 24
**JavaFX Version**: 21.0.6
**Maven Version**: 3.6+
