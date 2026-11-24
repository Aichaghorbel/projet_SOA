# Projet SOA - SmartSpace

Application Spring Boot de gestion des salles et des réservations pour les sessions d'examen. La branche `feature/grpc-room-reservation` contient :

- une API REST pour gérer les salles et les réservations ;
- une persistance JPA avec PostgreSQL ;
- un contrat gRPC décrit dans `smartspace/src/main/proto/room.proto` ;
- une règle métier empêchant le chevauchement des réservations pour une même salle.

## Architecture

Le module principal est `smartspace` :

```text
smartspace/
├── src/main/java/com/example/smartspace/
│   ├── controller/       # Endpoints REST
│   ├── model/            # Entités Room et Reservation
│   ├── repository/       # Repositories Spring Data JPA
│   └── service/          # Logique métier
├── src/main/proto/       # Contrat gRPC RoomService
└── src/main/resources/
    ├── application.properties
    └── db/init-room.sql
Prérequis
Java 17 ou supérieur ;
Maven 3.8+ ou le wrapper Maven fourni ;
PostgreSQL accessible sur localhost:5433 ;
une base de données nommée Educampus_db.

Configuration PostgreSQL
La configuration par défaut se trouve dans 
smartspace/src/main/resources/application.properties

spring.datasource.url=jdbc:postgresql://localhost:5433/Educampus_db
spring.datasource.username=postgres
spring.datasource.password=1234
spring.datasource.driver-class-name=org.postgresql.Driver


Configuration PostgreSQL
La configuration par défaut se trouve dans :
smartspace/src/main/resources/application.properties
