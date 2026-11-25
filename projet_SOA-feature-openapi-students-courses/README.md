# EduCampus – Academic Service

> Sprint 1 · Issue 1 · **Membre 1 – REST** · Branche Git : `feature/openapi-students-courses`

Microservice REST **Academic** de la plateforme EduCampus. Il gère les ressources académiques (étudiants, cours) et expose une API versionnée `/api/v1`, décrite par un contrat OpenAPI écrit **avant** le code (approche contract-first).

---

## 1. Objectif de l'issue

Définir le contrat OpenAPI Students/Courses, scaffolder le service Academic avec Spring Boot et fusionner les fragments OpenAPI en un contrat final.

**État du projet** : le service Academic est complet côté code : entités, DTO, repositories, services, controllers REST, gestion centralisée des erreurs et documentation Swagger UI sont écrits et classés par package. Les endpoints `/api/v1/students` et `/api/v1/courses` fonctionnent dès que les tables du schéma SQL existent dans PostgreSQL (script du Membre 2).

## 2. Livrables

| Livrable | Fichier | Description | État |
|---|---|---|---|
| Contrat Students/Courses | `contracts/students-courses.yaml` | Contrat OpenAPI 3.0.3 des deux ressources : 10 endpoints, schémas, validations, erreurs | ✅ Terminé (valide) |
| Contrat fusionné final | `contracts/academic-openapi.yaml` | Contrat unique du service, généré par `merge_openapi.py` | ⏳ Contient Students/Courses ; à régénérer avec le fragment du Membre 2 |
| Projet Academic | `academic-service/` | Projet Maven Spring Boot scaffoldé, avec Dockerfile | ✅ Compile et démarre |
| Infrastructure Docker | `docker-compose.yml` | PostgreSQL + Academic Service, healthcheck et volume de données | ✅ Terminé |

Hors périmètre du Membre 1 : le script SQL officiel (Membre 2) et l'infra bonus Jaeger/Kafka/Zookeeper.

## 3. Technologies

| Domaine | Choix |
|---|---|
| Langage | Java 17 |
| Framework | Spring Boot 3.3.x (packages `jakarta.*`) |
| Build | Maven, packaging Jar |
| API | Spring Web (REST), versionnement `/api/v1` |
| Persistance | Spring Data JPA, `ddl-auto: validate` |
| Base de données | PostgreSQL 16 (dans Docker) |
| Validation | Spring Validation (Bean Validation) |
| Supervision | Spring Boot Actuator (`/actuator/health`) |
| Documentation API | springdoc-openapi : Swagger UI (`/swagger-ui.html`) et OpenAPI JSON (`/v3/api-docs`) |
| Tests | JUnit 5, Mockito, MockMvc |
| Contrat | OpenAPI 3.0.3 |
| Conteneurs | Docker, Docker Compose |

## 4. Structure du projet

    educampus/
    ├── contracts/
    │   ├── students-courses.yaml        # contrat Students/Courses
    │   ├── academic-openapi.yaml        # contrat fusionné final
    │   └── merge_openapi.py             # script de fusion des fragments
    ├── academic-service/
    │   ├── src/main/java/com/educampus/academic/
    │   │   ├── controller/              # StudentController, CourseController
    │   │   ├── service/                 # StudentService, CourseService
    │   │   ├── repository/              # StudentRepository, CourseRepository
    │   │   ├── entity/                  # Student, Course, Semester
    │   │   ├── dto/                     # Request/Response, PageResponse, ErrorResponse, FieldErrorDetail
    │   │   ├── exception/               # ResourceNotFoundException, DuplicateResourceException, GlobalExceptionHandler
    │   │   ├── config/                  # OpenApiConfig (Swagger UI)
    │   │   └── AcademicApplication.java
    │   ├── src/main/resources/
    │   │   ├── application.yml          # configuration commune
    │   │   ├── application-dev.yml      # profil développement
    │   │   └── application-prod.yml     # profil Docker / production
    │   ├── src/test/java/com/educampus/academic/
    │   │   ├── controller/              # tests des endpoints (MockMvc)
    │   │   ├── service/                 # tests de la logique métier (Mockito)
    │   │   ├── dto/                     # tests de validation des DTO
    │   │   └── exception/               # tests du gestionnaire d'erreurs
    │   ├── Dockerfile                   # build multi-stage (Maven puis JRE 17)
    │   ├── pom.xml
    │   └── README.md
    ├── samples/                         # student.json, course.json, local-test-schema.sql (test local)
    ├── db/                              # dossier monté dans PostgreSQL ; guide pour le Membre 2
    ├── pgadmin/servers.json             # serveur PostgreSQL pré-enregistré dans pgAdmin
    ├── docker-compose.yml
    ├── .env.example
    └── .gitignore

### 4.1 Détail des classes par package

**`controller/`** : les endpoints REST, conformes au contrat OpenAPI.

| Classe | Rôle |
|---|---|
| `StudentController` | `/api/v1/students` : liste paginée (`page`, `size`, `q`), lecture, création (`201` + `Location`), modification, suppression (`204`) |
| `CourseController` | `/api/v1/courses` : mêmes opérations, avec le filtre `semester` en plus |

Les paramètres sont validés : `page` ≥ 0, `size` de 1 à 100, `id` ≥ 1 ; sinon l'API répond `400`.

**`service/`** : la logique métier.

| Classe | Rôle |
|---|---|
| `StudentService` | CRUD des étudiants ; `409` si l'email ou le numéro étudiant existe déjà ; `404` si l'étudiant est absent |
| `CourseService` | CRUD des cours ; `409` si le code existe déjà ; `404` si le cours est absent ; choisit la bonne requête selon les filtres `semester` et `q` |

**`repository/`** : l'accès aux données (Spring Data JPA).

| Interface | Méthodes principales |
|---|---|
| `StudentRepository` | `existsByEmail`, `existsByStudentNumber` et leurs variantes `...AndIdNot` pour le PUT ; `search(q, pageable)` |
| `CourseRepository` | `existsByCode` / `existsByCodeAndIdNot` ; `findBySemester`, `search`, `searchBySemester` |

**`entity/`** : le modèle persistant. Les noms de tables et de colonnes doivent correspondre au schéma SQL du Membre 2 (voir `db/README.md`).

| Classe | Rôle |
|---|---|
| `Student` | Entité JPA, table `students` ; dates de création et de modification renseignées automatiquement |
| `Course` | Entité JPA, table `courses` ; même gestion des dates |
| `Semester` | Enum des semestres S1 à S6 (stocké en texte) |

**`dto/`** : les objets échangés avec les clients, alignés sur les schémas du contrat.

| Classe | Rôle |
|---|---|
| `StudentRequest` / `CourseRequest` | Corps des POST/PUT, avec toutes les validations (`@NotBlank`, `@Email`, `@Past`, `@Min`/`@Max`, `@Pattern`...) |
| `StudentResponse` / `CourseResponse` | Réponses ; `from(entité)` convertit l'entité en DTO |
| `PageResponse` | Réponse paginée des listes (`content`, `page`, `size`, `totalElements`, `totalPages`) |
| `ErrorResponse` | Format unique des erreurs (timestamp, status, error, message, path, details) |
| `FieldErrorDetail` | Un champ invalide et son message, dans `details` des erreurs 400 |

**`exception/`** : la gestion centralisée des erreurs.

| Classe | Rôle |
|---|---|
| `ResourceNotFoundException` | Ressource absente, traduite en `404` |
| `DuplicateResourceException` | Doublon, traduit en `409` |
| `GlobalExceptionHandler` | `@RestControllerAdvice` : renvoie toujours un `ErrorResponse` ; `400` (validation, JSON illisible, paramètre invalide), `404`, `409` (dont violation d'intégrité en base), `500` sans détail technique |

**`config/`** : `OpenApiConfig` définit le titre et la description affichés dans Swagger UI.

### 4.2 Tests automatiques

Lancer : `mvn test` dans `academic-service`. Aucun test ne nécessite PostgreSQL.

| Test | Ce qu'il vérifie |
|---|---|
| `StudentRequestValidationTest`, `CourseRequestValidationTest` | Validations des DTO (email, niveau, date de naissance, format du code, crédits, champs obligatoires) |
| `StudentServiceTest`, `CourseServiceTest` | Règles métier avec repository simulé : création, doublons (409), absence (404), modification, suppression, choix de la requête de liste |
| `StudentControllerTest`, `CourseControllerTest` | Codes HTTP des endpoints : 200, 201 + `Location`, 204, 400, 404, 409 |
| `GlobalExceptionHandlerTest` | Format `ErrorResponse` et codes 400, 404, 409, 500 |

Les repositories ne sont pas couverts par un test automatique : ils nécessitent les vraies tables ; ils sont exercés par les tests manuels de la section 9.

## 5. Le contrat OpenAPI

### 5.1 Endpoints

| Méthode | URI | Description | Codes de réponse |
|---|---|---|---|
| GET | `/api/v1/students` | Liste paginée des étudiants (`page`, `size`, `q`) | 200, 400, 500 |
| GET | `/api/v1/students/{id}` | Récupérer un étudiant | 200, 400, 404, 500 |
| POST | `/api/v1/students` | Créer un étudiant | 201 (+ `Location`), 400, 409, 500 |
| PUT | `/api/v1/students/{id}` | Modifier un étudiant (remplacement complet) | 200, 400, 404, 409, 500 |
| DELETE | `/api/v1/students/{id}` | Supprimer un étudiant | 204, 400, 404, 409, 500 |
| GET | `/api/v1/courses` | Liste paginée des cours (`page`, `size`, `semester`, `q`) | 200, 400, 500 |
| GET | `/api/v1/courses/{id}` | Récupérer un cours | 200, 400, 404, 500 |
| POST | `/api/v1/courses` | Créer un cours | 201 (+ `Location`), 400, 409, 500 |
| PUT | `/api/v1/courses/{id}` | Modifier un cours (remplacement complet) | 200, 400, 404, 409, 500 |
| DELETE | `/api/v1/courses/{id}` | Supprimer un cours | 204, 400, 404, 409, 500 |

### 5.2 Conventions REST appliquées

- Noms de ressources au pluriel, sans verbe dans les URI, versionnement `/api/v1`.
- Verbes HTTP standards : GET (lecture), POST (création), PUT (remplacement), DELETE (suppression).
- `201 Created` avec l'en-tête `Location` à la création, `204 No Content` à la suppression.
- `400` pour une requête invalide, `404` pour une ressource absente, `409` pour un conflit (doublon d'email, de numéro étudiant, de code cours, ou ressource encore référencée).
- Listes paginées : `page` (à partir de 0) et `size` (1 à 100, 20 par défaut).

Une fois le service lancé, la documentation est disponible sur **http://localhost:8081/swagger-ui.html** (interface interactive) et **http://localhost:8081/v3/api-docs** (JSON).

## 6. Configuration

### 6.1 Variables d'environnement

| Variable | Valeur par défaut | Rôle |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/academic_db` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | `academic` | Utilisateur de la base |
| `DB_PASSWORD` | `academic` | Mot de passe de la base |
| `SERVER_PORT` | `8081` | Port du service |
| `SPRING_PROFILES_ACTIVE` | `dev` | Profil Spring actif |
| `JPA_DDL_AUTO` | `none` | Mode Hibernate : `none` ou `validate` |

Dans Docker Compose, les identifiants viennent du fichier `.env` (copié depuis `.env.example`). Le fichier `.env` est exclu de Git.

### 6.2 Profils

| Profil | Usage | Particularités |
|---|---|---|
| `dev` | Lancement local avec Maven | `show-sql: true`, logs `DEBUG` pour `com.educampus` |
| `prod` | Conteneur Docker | `show-sql: false`, logs `INFO` |

### 6.3 JPA

Hibernate ne crée jamais de table : le schéma SQL appartient au Membre 2. Le mode est piloté par la variable `JPA_DDL_AUTO` :

| Valeur | Quand l'utiliser |
|---|---|
| `none` (défaut) | Tant que le script SQL n'est pas chargé : le service démarre même si la base est vide |
| `validate` | Une fois les tables créées : Hibernate vérifie que les entités correspondent au schéma et refuse de démarrer sinon |

Pour passer en `validate` : `$env:JPA_DDL_AUTO="validate"; mvn spring-boot:run`.

## 7. Prérequis

Docker Desktop (ouvert avant de lancer les commandes), JDK 17, Maven 3.9+, Git, Python 3 avec `pip install pyyaml` (uniquement pour la fusion des contrats).

    java -version
    mvn -version
    docker compose version

## 8. Configuration et lancement (PowerShell)

**Étape 1 – Variables d'environnement** (à la racine du projet)

    copy .env.example .env

**Étape 2 – Démarrer PostgreSQL dans Docker**

    docker compose up -d postgres-academic
    docker compose ps
    docker compose logs --tail 20 postgres-academic

Résultat attendu : le conteneur `educampus-postgres-academic` est `Up ... (healthy)`, et les logs affichent `database system is ready to accept connections`.

**Étape 3 – Compiler et lancer Spring Boot** (dans `academic-service`, où se trouve le `pom.xml`)

    cd academic-service
    mvn clean install
    mvn spring-boot:run

Résultat attendu : `BUILD SUCCESS`, puis `Started AcademicApplication` dans les logs.

**Étape 4 – Tester le service** (dans un deuxième terminal)

    curl.exe http://localhost:8081/actuator/health

Résultat attendu : `"status":"UP"` et `"db":{"status":"UP"}`, ce qui valide la connexion entre Spring Boot et PostgreSQL.

**Étape 5 (alternative) – Tout lancer avec Docker**

    cd ..
    docker compose up -d --build
    docker compose logs --tail 30 academic-service
    curl.exe http://localhost:8081/actuator/health

**Étape 6 – Valider le contrat OpenAPI**

Ouvrir https://editor.swagger.io, puis File → Import file et choisir `contracts/students-courses.yaml`. Aucune erreur ne doit apparaître, et les 10 endpoints doivent s'afficher.

**Arrêt**

    docker compose down        # arrête les conteneurs, garde les données
    docker compose down -v     # arrête et supprime aussi la base

## 9. Tester les API

### 9.1 Prérequis

1. PostgreSQL démarré : `docker compose up -d postgres-academic`.
2. Les tables `students` et `courses` doivent exister.
   - **Avec le script du Membre 2** : copier `init-academic.sql` dans `db/`, puis `docker compose down -v` et `docker compose up -d postgres-academic`.
   - **En attendant, pour un test local uniquement** : charger le schéma de test (ne pas le copier dans `db/`, ne pas le présenter comme le schéma officiel) :

        Get-Content samples\local-test-schema.sql | docker exec -i educampus-postgres-academic psql -U academic -d academic_db

     Avant de charger le script du Membre 2, supprimer ce schéma avec `docker compose down -v`.
3. Service lancé : `cd academic-service` puis `mvn spring-boot:run`.

Sans tables, les endpoints répondent `500` (table inexistante) : ce n'est pas un bug du code.

### 9.2 Avec Swagger UI (le plus simple)

Ouvrir **http://localhost:8081/swagger-ui.html**, choisir un endpoint, cliquer sur **Try it out** puis **Execute**. L'interface est servie par le service lui-même : il n'y a pas de problème CORS.

### 9.3 Avec curl (PowerShell)

Les corps de requête sont dans `samples/`. Utiliser `curl.exe` (et non `curl`, qui est un alias PowerShell).

    curl.exe -i -X POST http://localhost:8081/api/v1/students -H "Content-Type: application/json" -d "@samples/student.json"
    curl.exe -i http://localhost:8081/api/v1/students
    curl.exe -i http://localhost:8081/api/v1/students/1
    curl.exe -i -X PUT http://localhost:8081/api/v1/students/1 -H "Content-Type: application/json" -d "@samples/student.json"
    curl.exe -i -X DELETE http://localhost:8081/api/v1/students/1

    curl.exe -i -X POST http://localhost:8081/api/v1/courses -H "Content-Type: application/json" -d "@samples/course.json"
    curl.exe -i "http://localhost:8081/api/v1/courses?semester=S5"
    curl.exe -i http://localhost:8081/api/v1/courses/1

### 9.4 Scénarios et résultats attendus

| Scénario | Commande | Résultat attendu |
|---|---|---|
| Créer un étudiant | `POST /api/v1/students` avec `student.json` | `201` + en-tête `Location: /api/v1/students/1` |
| Recréer le même étudiant | même `POST` une seconde fois | `409` (email déjà utilisé) |
| Lister | `GET /api/v1/students` | `200`, `content` avec l'étudiant, `totalElements` = 1 |
| Rechercher | `GET /api/v1/students?q=amine` | `200`, l'étudiant est trouvé |
| Lire un étudiant absent | `GET /api/v1/students/999` | `404` au format `ErrorResponse` |
| Corps invalide | `POST /api/v1/students` avec `-d "{}"` | `400` avec `details` listant les champs |
| Identifiant invalide | `GET /api/v1/students/abc` | `400` |
| Taille de page trop grande | `GET /api/v1/students?size=1000` | `400` |
| Modifier | `PUT /api/v1/students/1` | `200` avec les nouvelles valeurs |
| Supprimer | `DELETE /api/v1/students/1` | `204` sans contenu |
| Semestre inconnu | `GET /api/v1/courses?semester=S9` | `400` |
| Filtrer les cours | `GET /api/v1/courses?semester=S5` | `200`, uniquement les cours du semestre S5 |

## 10. Consulter la base avec pgAdmin

pgAdmin est inclus dans le `docker-compose.yml` : rien à installer.

    docker compose up -d postgres-academic pgadmin

Ouvrir **http://localhost:5050** : le serveur **EduCampus Academic** est déjà enregistré (mot de passe si demandé : `academic`).

| Information | Valeur |
|---|---|
| Nom de la base | `academic_db` |
| Utilisateur / mot de passe | `academic` / `academic` |
| Port | `5432` |

Chemin : Servers → EduCampus Academic → Databases → academic_db → Schemas → public → Tables.

Le guide destiné au script SQL (champs, contraintes, commandes de test) est dans `db/README.md`.
