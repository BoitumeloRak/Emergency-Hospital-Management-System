# Use the official OpenJDK 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file
COPY target/*.jar app.jar

# Set the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]