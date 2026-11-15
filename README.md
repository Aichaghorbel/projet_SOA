
# SOA Microservices - Docker Infrastructure

## 1. Project Overview

This project provides the Docker infrastructure for the SOA microservices architecture.

The infrastructure includes:

- PostgreSQL: Three independent databases.
- Apache Kafka: Asynchronous communication between microservices.
- Apache ZooKeeper: Kafka coordination.
- Jaeger: Distributed tracing.

## 2. Technologies

| Technology | Version |
|---|---|
| Docker | Docker Compose V2 |
| Java | 21 |
| Spring Boot | Project-defined version |
| Maven | 3.9 |
| PostgreSQL | 17 |
| Kafka | Confluent Platform 7.9.11 |
| ZooKeeper | Confluent Platform 7.9.11 |
| Jaeger | 2.23 |

## 3. Prerequisites

Before starting, install:

- Docker Desktop
- Git
- Java 21 (for local development)
- IntelliJ IDEA (recommended)

Make sure Docker Desktop is running.

## 4. Project Structure

```text
projet_SOA/
│
├── docker-compose.yml
├── Dockerfile
├── .dockerignore
├── .env
├── .env.example
├── .gitignore
├── README.md
│
└── postman/
    └── infrastructure-collection.json
```

## 5. Environment Configuration

Create your local environment file.

### Windows CMD

```cmd
copy .env.example .env
```

### Linux / macOS

```bash
cp .env.example .env
```

Configure the required environment variables in `.env`.

Never commit `.env` or share real passwords.

## 6. Start the Infrastructure

First, validate the Docker Compose configuration:

```bash
docker compose config
```

Download the required images:

```bash
docker compose pull
```

Start all infrastructure services:

```bash
docker compose up -d
```

## 7. Verify Containers

Display the status of all containers:

```bash
docker compose ps
```

Check PostgreSQL health:

```bash
docker exec postgres-students pg_isready
docker exec postgres-courses pg_isready
docker exec postgres-enrollments pg_isready
```

Check service logs:

```bash
docker compose logs -f
```

## 8. Services Access

| Service | Address |
|---|---|
| Students PostgreSQL | localhost:5433 |
| Courses PostgreSQL | localhost:5434 |
| Enrollments PostgreSQL | localhost:5435 |
| Kafka | localhost:9092 |
| ZooKeeper | localhost:2181 |
| Jaeger UI | http://localhost:16686 |
| Jaeger OTLP gRPC | localhost:4317 |
| Jaeger OTLP HTTP | localhost:4318 |

## 9. Kafka Verification

Check Kafka broker readiness:

```bash
docker exec kafka cub kafka-ready -b kafka:29092 1 20
```

Create a test topic:

```bash
docker exec kafka kafka-topics \
  --bootstrap-server kafka:29092 \
  --create \
  --topic test-topic \
  --partitions 1 \
  --replication-factor 1
```

List Kafka topics:

```bash
docker exec kafka kafka-topics \
  --bootstrap-server kafka:29092 \
  --list
```

## 10. Jaeger Verification

Open your browser:

http://localhost:16686

Jaeger provides distributed tracing to monitor requests across microservices.

## 11. Postman Collection

Import the following file into Postman:

```text
postman/infrastructure-collection.json
```

It contains initial requests for:

- Students Service
- Courses Service
- Enrollments Service
- Jaeger

Update the service URLs and endpoints according to the actual microservices configuration.

## 12. Stop Infrastructure

Stop containers without deleting persistent data:

```bash
docker compose down
```

Restart the infrastructure:

```bash
docker compose up -d
```

To stop containers and delete database volumes:

```bash
docker compose down -v
```

WARNING: This command permanently deletes the database data stored in these Docker volumes.

## 13. Security

- Never commit `.env`.
- Use `.env.example` to document required variables.
- Do not hardcode credentials in Docker Compose.
- Use local development credentials only.
- Configure secure credentials for production deployments.

## 14. Dockerfile Template

The root Dockerfile is a reusable multi-stage build template for Spring Boot applications using Maven and Java 21.

Adapt it to each microservice's Maven project structure before building.

Example:

```bash
docker build -t students-service:1.0 .
```

## 15. Team Workflow

Each developer should:

1. Pull the latest changes.
2. Create or switch to their feature branch.
3. Configure their local `.env`.
4. Start Docker infrastructure.
5. Develop and test their microservice.
6. Push their changes and create a Pull Request.
