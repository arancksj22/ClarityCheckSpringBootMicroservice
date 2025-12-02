# --- Stage 1: Build the Application ---
# We use a Maven image to build the JAR file
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the project files into the container
COPY . .

# Build the app (skipping tests to avoid environment errors during build)
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
# We use a lightweight Java JDK image to run the app
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the built JAR from the previous stage
# IMPORTANT: Ensure your target jar name matches.
# If your pom.xml says <artifactId>Backend</artifactId>, this is correct.
COPY --from=build /app/target/Backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port (Render will map this to the outside world)
EXPOSE 8080

# Command to start the app
ENTRYPOINT ["java", "-jar", "app.jar"]