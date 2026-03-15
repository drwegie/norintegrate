# Stage 1: Builder
FROM gradle:8.14.4-jdk25 AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build files first for layer caching
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY norintegrate-common/build.gradle.kts norintegrate-common/
COPY norintegrate-api/build.gradle.kts norintegrate-api/
COPY norintegrate-mcp/build.gradle.kts norintegrate-mcp/

# Download dependencies (cached layer if build files unchanged)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY norintegrate-common/src norintegrate-common/src
COPY norintegrate-api/src norintegrate-api/src

# Build the fat JAR
RUN ./gradlew :norintegrate-api:bootJar -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /workspace/norintegrate-api/build/libs/*.jar app.jar

# Set ownership
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
