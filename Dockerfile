# Use Eclipse Temurin JDK 21 as base image
FROM maven:3.9.11-amazoncorretto-21-debian

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw ./mvnw

# Build the application with shaded JAR
RUN ./mvnw clean package -Dshade -DskipTests

# Health check command
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD pgrep -f "java.*app.jar" || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "target/lib-1.0-SNAPSHOT.jar"]