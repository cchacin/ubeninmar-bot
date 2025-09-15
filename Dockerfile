# Use Eclipse Temurin JDK 21 as base image
FROM maven:3.9-eclipse-temurin-17

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn ./.mvn

# Build the application with shaded JAR
RUN mvn clean spotless:apply package -Dshade -DskipTests

# Create non-root user for security
#RUN groupadd -r botuser && useradd -r -g botuser botuser

# Copy the built JAR
#RUN cp /app/target/lib-1.0-SNAPSHOT.jar app.jar

# Change ownership to non-root user
#RUN chown -R botuser:botuser /app

# Switch to non-root user
#USER botuser

# Health check command
#HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
#  CMD pgrep -f "java.*app.jar" || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]