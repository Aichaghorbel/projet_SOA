# --- Build ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Pas de "dependency:go-offline" (fragile) ni de -q : les vraies erreurs restent visibles.
# Le cache BuildKit évite de retélécharger les dépendances à chaque build.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean package -DskipTests \
        -Dmaven.wagon.http.retryHandler.count=5 \
        -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

# --- Run ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/academic-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
