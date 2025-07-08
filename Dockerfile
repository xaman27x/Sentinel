# =========================
# Step 1: Build Frontend
# =========================
#FROM node:20-alpine AS frontend-builder

# WORKDIR /app/frontend
# COPY frontend/ ./

# RUN npm install
# RUN npm run build


# =========================
# Step 2: Build Backend
# =========================
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

WORKDIR /app/backend
COPY backend/ ./

# Copy frontend build into Spring Boot static resources
# COPY --from=frontend-builder /app/frontend/dist/ src/main/resources/static/

RUN mvn clean package -DskipTests


# =========================
# Step 3: Create Production Image
# =========================
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=backend-builder /app/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]
