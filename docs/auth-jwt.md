# Authentification JWT et API Gateway

Dans l'architecture SOA EduCampus, les clients externes (front-end, Swagger, curl, Postman) appellent **uniquement l'API Gateway (port 8085)**.

L'API Gateway vérifie le token JWT et route le trafic HTTP REST :
- Vers **`auth-service` (8084)** pour la connexion et la délivrance des tokens.
- Vers **`academic-service` (8081)** pour toutes les opérations métier REST.

Le service **`academic-service` (REST)** est l'**orchestrateur métier** : c'est lui qui appelle en interne les services en aval :
- Le service **SOAP** (`university-administration` : 8083) pour la vérification d'éligibilité et la soumission de certifications.
- Le service **gRPC** (`smartspace` : 9090) pour la réservation de salles et le streaming de correction d'examens.

```
Client ──► API Gateway (8085) ──► auth-service (8084)          : /auth/**
              │ filtre JWT
              └────────────────► academic-service REST (8081)  : /api/**
                                       │
                                       ├──(SOAP)──► University Administration (8083) : Éligibilité, Certification
                                       └──(gRPC)──► Smart Space & Exam (9090)        : Salles, Correction d'examens
```

---

## 1. Ports des services

| Service | Port | Protocole | Rôle |
|---|:---:|:---:|---|
| **API Gateway** | `8085` | HTTP | Point d'entrée unique, filtre JWT & RBAC |
| **auth-service** | `8084` | REST | Login, génération des tokens JWT HS256 |
| **academic-service** | `8081` | REST | Orchestrateur métier (étudiants, cours, inscriptions, résultats) |
| **university-administration** | `8083` | SOAP | Éligibilité et certification (appelé par Academic en SOAP) |
| **Smart Space & Exam** | `9090` | gRPC | Salles et correction d'examen (appelé par Academic en gRPC) |

---

## 2. Adresses utiles

| Adresse | Usage |
|---|---|
| `http://localhost:8085/auth/login` | Login via la Gateway |
| `http://localhost:8085/swagger-ui.html` | Swagger d'Academic via la Gateway (bouton **Authorize**) |
| `http://localhost:8084/swagger-ui/index.html` | Swagger direct du service Auth |

---

## 3. Utilisateurs de test (Développement)

| Utilisateur | Mot de passe | Rôle | Droits |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN` | Accès complet (lecture et écriture) |
| `teacher` | `teacher123` | `TEACHER` | Accès complet (lecture et écriture) |
| `student` | `student123` | `STUDENT` | Lecture globale + inscription cours (`POST /enrollments`) + demande certification (`POST /certification-requests`) |

---

## 4. Login

`POST http://localhost:8085/auth/login`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Réponse `200 OK` :
```json
{
  "accessToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "roles": ["ADMIN"]
}
```

---

## 5. Règles d'accès (RBAC) appliquées par la Gateway

| Requête | Qui | Sinon |
|---|---|---|
| `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**` | **Public** (tout le monde) | Autorisé directement |
| `GET`, `HEAD` sur `/api/**` | **Tout utilisateur connecté** | `401 Unauthorized` |
| `POST`, `PUT`, `PATCH`, `DELETE` sur `/api/**` | `ADMIN`, `TEACHER` | `403 Forbidden` |
| `POST /api/v1/certification-requests` | `ADMIN`, `TEACHER`, `STUDENT` | `403 Forbidden` |
| `POST /api/v1/enrollments` | `ADMIN`, `TEACHER`, `STUDENT` | `403 Forbidden` |

La Gateway supprime systématiquement les en-têtes `X-User` et `X-Roles` envoyés par le client, puis les recrée à partir du token JWT vérifié pour éviter toute usurpation d'identité.

---

## 6. Variables d'environnement

| Variable | Défaut | Rôle |
|---|---|---|
| `JWT_SECRET` | valeur de développement | Secret HS256, **au moins 32 caractères, identique** dans auth-service et api-gateway |
| `JWT_EXPIRATION_MINUTES` | `15` | Durée de vie du token |
| `GATEWAY_PORT` | `8085` | Port de la Gateway |
| `AUTH_SERVICE_URL` | `http://localhost:8084` | Adresse du service Auth |
| `ACADEMIC_SERVICE_URL` | `http://localhost:8081` | Adresse d'Academic |

---

## 7. Lancer avec Docker Compose

L'ensemble des services, y compris `auth-service` et `api-gateway`, sont conteneurisés et orchestrés par Docker Compose.

```bash
# 1. Copier le fichier d'environnement
cp .env.example .env

# 2. Démarrer toute l'infrastructure
docker compose up -d --build

# 3. Vérifier les conteneurs
docker compose ps
```
