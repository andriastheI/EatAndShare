# 🍽️ EatAndShare — Developer Documentation

## 📌 Overview
**EatAndShare** is a recipe-sharing web platform built using **Spring Boot**, **Thymeleaf**, and **MySQL**.

Users can:
- Register and log in securely  
- Upload recipes (including images)  
- Add structured ingredients (name, quantity, unit)  
- Browse and search recipes by category  


## 🛠️ Installation Instructions

### 1. Install Java 21
Download and install Java 21:  
https://www.oracle.com/java/technologies/downloads/

### 2. Install MySQL 8.0.43
Download MySQL 8.0.43:  
https://downloads.mysql.com/archives/installer/


### 3. Clone the Project

git clone https://github.com/andriastheI/EatAndShare.git
cd EatAndShare

4. Configure MySQL
Make sure MySQL is running and configured with the correct database, username, and password.

5. Verify Application Configuration
Check the file: src/main/resources/application.properties if it has the same configuration as the following

spring.datasource.url=jdbc:mysql://localhost:3306/cookbook
spring.datasource.username=blackfe
spring.datasource.password=iampassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
# Image Upload Directory
upload.dir=uploads

Ensure it is properly configured for your local environment.

🚀 Running the Application (Gradle Wrapper)
🐧 Linux / macOS

Open a terminal in the project directory:
cd path/to/EatAndShare

Give execution permission to the Gradle wrapper (only once):
chmod +x gradlew

Run the application:
./gradlew bootRun


🪟 Windows (Command Prompt or PowerShell)

Open a terminal in the project directory:
cd path/to/EatAndShare

Run the Windows Gradle wrapper:
gradlew.bat bootRun

🌐 Accessing the Application
Once the server starts, open your browser and navigate to:

👉 http://localhost:8080

For additional details about using the web application, please refer to the User Manual.
