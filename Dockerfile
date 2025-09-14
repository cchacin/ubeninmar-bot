# Use Eclipse Temurin JDK 21 as base image
FROM maven:3.9.11-amazoncorretto-21-al2023 AS builder

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the application with shaded JAR
RUN mvn clean package -Dshade -DskipTests

# Runtime stage with minimal JRE
FROM eclipse-temurin:21-jre

# Create non-root user for security
RUN groupadd -r botuser && useradd -r -g botuser botuser

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/lib-1.0-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown -R botuser:botuser /app

# Switch to non-root user
USER botuser

# Health check command
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD pgrep -f "java.*app.jar" || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]