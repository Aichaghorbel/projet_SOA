# Intégration Academic (REST) → University Administration (SOAP)

Ce document explique la partie de l'intégration où le service **Academic** (REST, port 8081) appelle le service **University Administration** (SOAP, port 8083) pour traiter une demande de certification, et comment la faire fonctionner dans Docker.

---

## 1. Vue d'ensemble

```
Client (Swagger / curl)
   │  POST /api/v1/certification-requests   (JSON)
   ▼
Academic-service  (REST, 8081)
   │  1. vérifie l'étudiant en base
   │  2. CheckEligibility      (SOAP, XML)  ──┐
   │  3. SubmitCertification   (SOAP, XML)  ──┤
   ▼                                         ▼
University-administration  (SOAP, 8083, /ws)
   │  vérifie le statut de l'étudiant, enregistre la demande
   ▼
Réponse SOAP  →  Academic  →  Réponse JSON au client (202, 422 ou 503)
```

Les deux services communiquent par **SOAP** : Academic envoie une enveloppe XML à l'administration et lit sa réponse XML. Le client, lui, ne voit que du JSON.

---

## 2. Le contrat partagé (XSD)

Le contrat est un fichier XSD, identique dans les deux services :

- `projet_SOA-feature-admin-data-model/university-administration/src/main/resources/xsd/university-administration.xsd` (côté administration, fournisseur)
- `projet_SOA-feature-openapi-students-courses/academic-service/src/main/resources/xsd/university-administration.xsd` (côté Academic, consommateur)

Namespace : `http://educampus.com/soap/university-administration`

Opérations utilisées :

| Opération | Requête | Réponse | Rôle |
|---|---|---|---|
| `CheckEligibility` | `studentId`, `academicYear` (optionnel) | `eligible`, `reason` | Vérifier que l'étudiant peut recevoir un certificat |
| `SubmitCertification` | `studentId`, `certificationType`, `comment` (optionnel) | `requestId`, `status`, `submittedAt` | Enregistrer la demande |

Le test `XsdContractSyncTest` compare les deux fichiers XSD et échoue s'ils divergent.

**Pourquoi le XSD est important :** Academic génère ses classes Java à partir de ce fichier (plugin `jaxb-maven-plugin`, package `com.educampus.academic.soap.generated`). Si le contrat change côté administration, il faut le recopier côté Academic et recompiler.

---

## 3. Le parcours d'une demande, étape par étape

Exemple : un client demande un certificat de réussite pour l'étudiant 2 (ETU2025102).

**Étape 1 : réception REST.** `CertificationController` reçoit le JSON :

```json
{ "studentId": 2, "certificationType": "SUCCESS_CERTIFICATE" }
```

Les validations Bean Validation s'appliquent (`studentId` ≥ 1, `certificationType` non vide).

**Étape 2 : vérification locale.** `CertificationRequestService` :
- convertit `certificationType` en valeur de l'énumération SOAP (sinon erreur 400) ;
- cherche l'étudiant en base avec `StudentRepository` (sinon erreur 404) ;
- récupère son **numéro étudiant** (`studentNumber`, par exemple `ETU2025102`). C'est cet identifiant qui est partagé avec l'administration.

**Étape 3 : CheckEligibility.** Academic envoie :

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <CheckEligibilityRequest xmlns="http://educampus.com/soap/university-administration">
      <studentId>ETU2025102</studentId>
    </CheckEligibilityRequest>
  </soap:Body>
</soap:Envelope>
```

Si la réponse contient `eligible = false`, Academic arrête le traitement et renvoie **422**.

**Étape 4 : SubmitCertification.** Si l'étudiant est éligible, Academic envoie la demande. La réponse contient le `requestId` généré par l'administration, par exemple `REQ-...`.

**Étape 5 : réponse REST.** Academic renvoie **202 Accepted** avec le `requestId`, le statut et la date de soumission.

---

## 4. Ce que fait l'administration

Côté administration (`EligibilityService`, `CertificationService`) :

- **Éligibilité** : un étudiant inconnu, suspendu, retiré ou diplômé produit une SOAP Fault, c'est-à-dire un refus métier.
- **Soumission** : une demande en double (même étudiant, même type, déjà en cours) produit une SOAP Fault `DuplicateCertification`, ce qui donne **422** côté Academic.
- **Stockage** : les données sont en mémoire. Un redémarrage du conteneur `educampus-soap-admin` efface les demandes.

Étudiants de test ajoutés pour l'intégration : `ETU2025101` et `ETU2025102`, statut `ACTIVE`.

---


## 6. Gestion des erreurs

| Situation | Exception Java | Code HTTP | Message |
|---|---|---|---|
| Type de certification inconnu | `InvalidCertificationTypeException` | 400 | Liste des valeurs autorisées |
| Étudiant absent en base | `ResourceNotFoundException` | 404 | Étudiant introuvable |
| Étudiant non éligible (SOAP Fault) | `UniversityAdministrationRejectedException` | 422 | Message renvoyé par l'administration |
| Demande en double (SOAP Fault) | `UniversityAdministrationRejectedException` | 422 | « déjà en cours » |
| Délai de lecture dépassé (5 s) | `UniversityAdministrationUnavailableException` | 503 | « délai dépassé en attendant l'administration » |
| Service arrêté ou injoignable | `UniversityAdministrationUnavailableException` | 503 | « Service d'administration indisponible » |

**Pourquoi deux types de 503 :** un service arrêté refuse la connexion immédiatement. Un service en pause ou très lent accepte la connexion, mais ne répond pas : c'est le délai de lecture qui se déclenche.

**Pas de nouvel essai automatique sur la soumission.** Si l'administration a reçu la demande avant le timeout, un nouvel envoi créerait un doublon. C'est au client de réessayer, en connaissance de cause.

---

## 7. Configuration des timeouts

Dans `application.yml` :

```yaml
university-admin:
  url: ${UNIVERSITY_ADMIN_URL:http://localhost:8083/ws}
  connect-timeout-ms: 2000
  read-timeout-ms: 5000
```

Dans `docker-compose.yml`, le service Academic reçoit :

```yaml
UNIVERSITY_ADMIN_URL: http://soap-university-administration:8083/ws
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WS_CLIENT: TRACE
```

Le nom `soap-university-administration` est le nom du service dans le réseau Docker. En local, hors Docker, l'URL est `http://localhost:8083/ws`.

---

## 8. Comment faire l'intégration réelle

### 8.1 Prérequis

- Docker Desktop lancé.
- Les ports 8081, 8083 et 8085 libres.
- Un fichier `body.json` dans le dossier courant :

```json
{ "studentId": 2, "certificationType": "SUCCESS_CERTIFICATE" }
```

### 8.2 Démarrer la stack

```powershell
cd projet_SOA-feature-docker-env
docker compose up -d --build
docker compose ps
```

Attendez que `rest-academic-service` affiche la ligne `Started` :

```powershell
docker compose logs rest-academic-service | Select-String "Started"
```

### 8.3 Vérifier que les deux services répondent

```powershell
curl.exe http://localhost:8083/ws/university-administration.wsdl
curl.exe http://localhost:8081/actuator/health
```

Le WSDL doit s'afficher en XML. La santé doit renvoyer `UP`.

### 8.4 Envoyer une demande réelle

```powershell
curl.exe -i -X POST http://localhost:8081/api/v1/certification-requests -H "Content-Type: application/json" -d "@body.json"
```

**Résultat attendu :** `HTTP/1.1 202` avec un `requestId` généré par l'administration.

### 8.5 Voir les enveloppes SOAP échangées

```powershell
docker compose logs rest-academic-service | Select-String "Sent request|Received response"
```

Vous voyez les requêtes et les réponses XML. `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WS_CLIENT: TRACE` active ce log.

### 8.6 Tester les cas d'erreur

| Test | Commande | Résultat attendu |
|---|---|---|
| Demande en double | relancer le POST | 422 « déjà en cours » |
| Étudiant inconnu | `studentId: 99` | 404 |
| Type invalide | `certificationType: "FAUX"` | 400 |
| Administration en pause | `docker pause educampus-soap-admin` puis POST | 503 après environ 5 s (délai) |
| Administration arrêtée | `docker stop educampus-soap-admin` puis POST | 503 immédiat (indisponible) |

Après chaque test de panne :

```powershell
docker unpause educampus-soap-admin
# ou
docker start educampus-soap-admin
```

Attendez la ligne `Started` de l'administration avant de refaire un POST. Les demandes en mémoire sont perdues après un `stop`.







