# --- Build ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean package -DskipTests \
        -Dmaven.wagon.http.retryHandler.count=5 \
        -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

# --- Run ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/auth-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
