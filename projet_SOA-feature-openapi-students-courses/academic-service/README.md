# academic-service

Microservice REST **Academic** d'EduCampus (Java 17, Spring Boot 3.3.x, Maven, PostgreSQL).

## Lancer (PostgreSQL dans Docker : voir le README à la racine)

    mvn clean install
    mvn spring-boot:run

Vérification : http://localhost:8081/actuator/health  (doit afficher `UP`, avec `db: UP`)

## Variables d'environnement

| Variable | Défaut |
|---|---|
| DB_URL | jdbc:postgresql://localhost:5432/academic_db |
| DB_USERNAME | academic |
| DB_PASSWORD | academic |
| SERVER_PORT | 8081 |
| SPRING_PROFILES_ACTIVE | dev |

Sprint 1 : squelette uniquement. Le schéma SQL (Membre 2) est utilisé tel quel (`ddl-auto: validate`).
Le CRUD Students/Courses arrive aux sprints suivants.
