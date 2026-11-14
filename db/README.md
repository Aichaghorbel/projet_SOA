# Base de données Academic – guide pour le Membre 2

Le Membre 2 écrit le script SQL officiel : **`db/init-academic.sql`**.

## Informations de connexion

| Élément | Valeur |
|---|---|
| Nom de la base | `academic_db` |
| Utilisateur | `academic` |
| Mot de passe | `academic` (voir `.env`) |
| Port (depuis le PC) | `5432` |
| Hôte (depuis le PC) | `localhost` |
| Hôte (depuis Docker) | `postgres-academic` |
| pgAdmin | http://localhost:5050 |

## Voir et tester la base avec pgAdmin

1. `docker compose up -d postgres-academic pgadmin`
2. Ouvrir **http://localhost:5050** (pas de connexion demandée).
3. Le serveur **EduCampus Academic** est déjà enregistré. Si un mot de passe est demandé : `academic`.
4. Dérouler : Servers → EduCampus Academic → Databases → **academic_db** → Schemas → public → Tables.
5. Pour tester une requête : clic droit sur `academic_db` → **Query Tool**.

## Champs à reprendre du contrat (`contracts/students-courses.yaml`)

Proposition de correspondance (convention Spring/JPA : noms de colonnes en `snake_case`). Les types et contraintes sont à décider par le Membre 2, qui définit le modèle SQL.

**Table `students`**

| Champ du contrat | Colonne proposée | Règles du contrat |
|---|---|---|
| `id` | `id` | généré, clé primaire |
| `studentNumber` | `student_number` | obligatoire, unique, 4 à 20 caractères |
| `firstName` | `first_name` | obligatoire, 2 à 100 |
| `lastName` | `last_name` | obligatoire, 2 à 100 |
| `email` | `email` | obligatoire, unique, 150 max |
| `dateOfBirth` | `date_of_birth` | optionnel, date |
| `phone` | `phone` | optionnel, 20 max |
| `program` | `program` | optionnel, 100 max |
| `level` | `level` | obligatoire, de 1 à 5 |
| `createdAt` | `created_at` | généré |
| `updatedAt` | `updated_at` | généré |

**Table `courses`**

| Champ du contrat | Colonne proposée | Règles du contrat |
|---|---|---|
| `id` | `id` | généré, clé primaire |
| `code` | `code` | obligatoire, unique, format `SOA301` |
| `title` | `title` | obligatoire, 3 à 150 |
| `description` | `description` | optionnel, 1000 max |
| `credits` | `credits` | obligatoire, de 1 à 30 |
| `semester` | `semester` | obligatoire : S1 à S6 |
| `teacherName` | `teacher_name` | optionnel, 100 max |
| `capacity` | `capacity` | optionnel, de 1 à 500 |
| `createdAt` | `created_at` | généré |
| `updatedAt` | `updated_at` | généré |

Si le Membre 2 change un nom ou un type, il prévient le Membre 1 : le contrat OpenAPI et les entités JPA doivent rester alignés avec le SQL.

## Types attendus par les entités JPA (Membre 1)

Les entités `Student` et `Course` sont déjà écrites (`academic-service/.../entity/`). Pour que `ddl-auto: validate` passe, les types SQL doivent être compatibles :

| Champ Java | Type SQL attendu |
|---|---|
| `id` (Long) | `BIGINT` / `BIGSERIAL` (clé primaire auto-générée) |
| champs texte (String) | `VARCHAR(n)` avec la longueur du tableau ci-dessus |
| `level`, `credits`, `capacity` (Integer) | `INTEGER` (pas `SMALLINT`) |
| `dateOfBirth` (LocalDate) | `DATE` |
| `createdAt`, `updatedAt` (LocalDateTime) | `TIMESTAMP` (sans fuseau) |
| `semester` (enum) | `VARCHAR(2)` contenant `S1` à `S6` |

Noms de tables attendus : `students` et `courses`.

## Charger et tester le script

    copy <chemin>\init-academic.sql db\init-academic.sql
    docker compose down -v
    docker compose up -d postgres-academic pgadmin
    docker exec -it educampus-postgres-academic psql -U academic -d academic_db -c "\dt"

`down -v` est obligatoire : PostgreSQL ne rejoue le script qu'au premier démarrage d'un volume vide.

En cas d'erreur de syntaxe dans le script : `docker compose logs --tail 50 postgres-academic`.
