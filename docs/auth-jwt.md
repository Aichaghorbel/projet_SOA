# Authentification JWT et API Gateway

Les clients (front, Swagger, curl) appellent **uniquement la Gateway**. Elle vérifie le token JWT
avant de transmettre la requête aux services.

```
Client ──► API Gateway (8085) ──► auth-service (8084)      /auth/**
              │ filtre JWT
              └────────────────► academic-service (8081)    /api/**
```

## Ports

| Service | Port | Rôle |
|---|---|---|
| API Gateway | 8085 | Point d'entrée unique, filtre JWT |
| auth-service | 8084 | Login, génération des tokens |
| academic-service | 8081 | REST étudiants, cours, inscriptions, résultats |
| university-administration (SOAP) | 8083 | Éligibilité, certification |
| Smart Space (gRPC) | 9090 | Réservation de salle, correction d'examen |

## Adresses utiles

| Adresse | Usage |
|---|---|
| http://localhost:8085/swagger-ui.html | Swagger d'Academic via la Gateway (bouton **Authorize**) |
| http://localhost:8084/swagger-ui/index.html | Swagger du service Auth (login) |
| http://localhost:8084/v3/api-docs | Contrat OpenAPI du service Auth |

## Utilisateurs de test (développement uniquement)

| Utilisateur | Mot de passe | Rôle |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `teacher` | `teacher123` | TEACHER |
| `student` | `student123` | STUDENT |

Ces comptes sont définis dans `auth-service/src/main/resources/application.yml`. Ils n'existent que pour
le développement : ne jamais les utiliser en production.

## Login

`POST /auth/login` (public)

```json
{ "username": "admin", "password": "admin123" }
```

Réponse `200` :
```json
{ "accessToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 900, "roles": ["ADMIN"] }
```

Erreurs : `401` identifiants incorrects, `400` champ manquant.

Contenu du token (HS256) : `sub` = utilisateur, `roles` = liste de rôles, `iat`, `exp` (15 minutes par défaut).

## Utiliser le token

Envoyer l'en-tête `Authorization: Bearer <token>` à chaque appel vers `/api/**`.

PowerShell (depuis un dossier contenant `tmp-login.json`) :
```powershell
Set-Content tmp-login.json '{"username":"admin","password":"admin123"}'
$t = (curl.exe -s -X POST http://localhost:8085/auth/login -H "Content-Type: application/json" -d "@tmp-login.json" | ConvertFrom-Json).accessToken
curl.exe -i http://localhost:8085/api/v1/students -H "Authorization: Bearer $t"
```

## Règles d'accès (appliquées par la Gateway)

| Requête | Qui | Sinon |
|---|---|---|
| `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**` | tout le monde | |
| `GET`, `HEAD` sur `/api/**` | tout utilisateur connecté | `401` sans token valide |
| `POST`, `PUT`, `PATCH`, `DELETE` sur `/api/**` | `ADMIN`, `TEACHER` | `403` |
| `POST /api/v1/certification-requests` | `ADMIN`, `TEACHER`, `STUDENT` | `403` |

La Gateway supprime les en-têtes `X-User` et `X-Roles` envoyés par le client, puis les recrée à partir
du token. Un client ne peut donc pas usurper une identité.

## Exemples de réponses d'erreur

| Cas | Code |
|---|---|
| Pas d'en-tête `Authorization` | `401` |
| Token invalide, modifié ou expiré | `401` |
| Rôle insuffisant (ex. `student` fait un `POST /api/v1/enrollments`) | `403` |

## Configuration

| Variable | Défaut | Rôle |
|---|---|---|
| `JWT_SECRET` | valeur de développement | Secret HS256, **au moins 32 caractères, identique** dans auth-service et api-gateway |
| `JWT_EXPIRATION_MINUTES` | `15` | Durée de vie du token |
| `GATEWAY_PORT` | `8085` | Port de la Gateway |
| `AUTH_SERVICE_URL` | `http://localhost:8084` | Adresse du service Auth |
| `ACADEMIC_SERVICE_URL` | `http://localhost:8081` | Adresse d'Academic |

Le secret par défaut ne sert qu'au développement : en dehors, le définir dans `.env` (jamais commité).

## Lancer et arrêter

1. `docker compose up -d` (bases, Academic, SOAP, gRPC).
2. `cd auth-service` puis `mvn spring-boot:run`.
3. `cd api-gateway` puis `mvn spring-boot:run`.
4. Tester sur http://localhost:8085/swagger-ui.html.

Arrêt : `Ctrl+C` dans chaque terminal Maven, puis `docker compose down`.

## Tester dans Swagger

1. Sur http://localhost:8084/swagger-ui/index.html : `POST /auth/login`, copier `accessToken`.
2. Sur http://localhost:8085/swagger-ui.html : **Authorize**, coller le token (sans le mot `Bearer`).
3. Exécuter un endpoint : un `GET` doit donner `200`, un `POST` avec le rôle `student` doit donner `403`.

## Limites actuelles

- Utilisateurs en mémoire (pas de table `users`).
- Pas de refresh token : après 15 minutes, se reconnecter.
- `academic-service` reste joignable directement sur le port 8081 en local : seule la Gateway doit être exposée en production.