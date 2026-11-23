# EduCampus — Smart Space & Exam Management Microservice (gRPC + REST)

A cloud-native microservice built with **Spring Boot 3.3.5**, **gRPC 1.63.0**, **Protocol Buffers v3**, and **PostgreSQL 16**. It powers two core bounded contexts:
1. **Exam Management** (Automated MCQ and short-answer correction, real-time server-streaming progress, session lifecycle).
2. **Room & Reservation Management** (Space management, booking reservations, strict anti-overlap rule verification).

---

## 🏛️ Architecture Overview

The application follows Domain-Driven Design (DDD) and Clean Architecture principles, exposing both high-performance **gRPC** interfaces (HTTP/2 binary serialization) and standard **REST** endpoints (HTTP/1.1 JSON).

```
                           +--------------------------------------+
                           |             Clients                  |
                           |   (Postman, Web App, Mobile App)     |
                           +------------------+-------------------+
                                              |
                     gRPC (HTTP/2 :9090)      |      REST (HTTP/1.1 :8082)
                   +--------------------------+-------------------------+
                   |                                                    |
                   v                                                    v
     +---------------------------+                        +---------------------------+
     |     gRPC Controllers      |                        |      REST Controllers     |
     | - ExamGrpcService         |                        | - ExamController          |
     | - RoomGrpcService         |                        | - RoomController          |
     | - ReservationGrpcService  |                        | - ReservationController   |
     +-------------+-------------+                        +-------------+-------------+
                   |                                                    |
                   +--------------------------+-------------------------+
                                              |
                                              v
                               +-----------------------------+
                               |        Service Layer        |
                               | - ExamService               |
                               | - CorrectionService         |
                               | - RoomService               |
                               | - ReservationService        |
                               +--------------+--------------+
                                              |
                                              v
                               +-----------------------------+
                               |     Data Persistence Layer  |
                               |       (Spring Data JPA)     |
                               +--------------+--------------+
                                              |
                                              v
                               +-----------------------------+
                               |     PostgreSQL 16 (Docker)  |
                               |  - questions, exam_sessions |
                               |  - room, reservation        |
                               +-----------------------------+
```

---

## 📦 Tech Stack & Dependencies

| Component | Technology | Version | Description |
|---|---|---|---|
| Framework | **Spring Boot** | `3.3.5` | Core application framework |
| RPC Protocol | **gRPC Java** | `1.63.0` | High-performance RPC framework |
| Serialization | **Protocol Buffers** | `3.25.5` | Language-agnostic binary serialization |
| gRPC Spring Boot | **grpc-server-spring-boot-starter** | `3.1.0.RELEASE` | `@GrpcService` container integration |
| Database | **PostgreSQL** | `16-alpine` | Relational database (Docker container) |
| In-Memory DB | **H2 Database** | `2.x` | Used in PostgreSQL mode for automated test suites |
| Build Tool | **Maven** + `mvnw` wrapper | `3.9+` | Build & dependency management |
| JDK | **Java SE** | `17` / `21` | Runtime environment |

---

## 🛠️ Protobuf Service Contracts

Protobuf files are located under:
- `contracts/exam.proto` / `gRPC/src/main/proto/exam.proto` (Package: `educampus.exam.v1`, Java: `com.educampus.smartspace.grpc.exam`)
- `contracts/room.proto` / `gRPC/src/main/proto/room.proto` (Package: `smartspace`, Java: `com.educampus.smartspace.grpc.room`)

### 1. `educampus.exam.v1.ExamService`
| RPC Method | Type | Input Message | Output Message | Description |
|---|---|---|---|---|
| `SubmitExam` | Unary | `ExamSubmissionRequest` | `ExamSubmissionResponse` | Submits all student answers, executes automated correction, persists score & grade. |
| `StreamExamProgress` | Server Streaming | `ExamSubmissionRequest` | `stream ExamProgressEvent` | Corrects questions step-by-step and streams real-time progress events to client. |

### 2. `smartspace.RoomService`
| RPC Method | Type | Input Message | Output Message | Description |
|---|---|---|---|---|
| `CreateRoom` | Unary | `CreateRoomRequest` | `CreateRoomResponse` | Creates a new examination/classroom room. |
| `GetRoom` | Unary | `GetRoomRequest` | `Room` | Fetches room info and all its existing reservations. |
| `UpdateRoom` | Unary | `UpdateRoomRequest` | `UpdateRoomResponse` | Modifies room name, capacity, or location. |
| `DeleteRoom` | Unary | `DeleteRoomRequest` | `DeleteRoomResponse` | Deletes a room by ID. |
| `ListRooms` | Unary | `ListRoomsRequest` | `ListRoomsResponse` | Retrieves all registered rooms. |

### 3. `smartspace.ReservationService`
| RPC Method | Type | Input Message | Output Message | Description |
|---|---|---|---|---|
| `CreateReservation` | Unary | `CreateReservationRequest` | `CreateReservationResponse` | Reserves a room. Rejects overlapping slots with `Status.ABORTED`. |
| `GetReservation` | Unary | `GetReservationRequest` | `Reservation` | Fetches reservation details by ID. |
| `ListReservations` | Unary | `ListReservationsRequest` | `ListReservationsResponse` | Lists reservations (all or filtered by `room_id`). |
| `DeleteReservation` | Unary | `DeleteReservationRequest` | `DeleteReservationResponse` | Cancels/deletes a reservation by ID. |

---

## 🚀 How to Run the Project

### Step 1: Start PostgreSQL with Docker Compose
```bash
cd gRPC
docker compose up -d
```
This runs PostgreSQL on `localhost:5432` with database `smartspace_db` and pre-loads initial schema & seed data (`01-init-room.sql` and `02-init-exam.sql`).

### Step 2: Build and Run Application
```bash
./mvnw clean spring-boot:run
```
- gRPC Server starts on: **`localhost:9090`**
- HTTP REST Server starts on: **`http://localhost:8082`**

---

## 🧪 Testing in Postman (Examples & Scenarios)

> [!IMPORTANT]
> In Postman, create a new **gRPC Request**:
> 1. Set server address to `localhost:9090`.
> 2. Open **Settings** tab in Postman and select **No encryption** (Plaintext).
> 3. In **Service definition**, choose **Import a .proto file** and select `room.proto` and `exam.proto`.

---

### A. Exam Management Testing

#### 💡 Note on `exam_session_id`:
- Setting `"exam_session_id": 0` automatically creates a **new fresh session on-the-fly**, so you never run into duplicate session collisions.
- If you use an existing session ID that is already `COMPLETED`, the server returns a clean `INVALID_ARGUMENT` preventing duplicate submissions.
- Re-submitting an in-progress session performs an **upsert** (updating your answers safely).

---

#### Test A1: `SubmitExam` (Unary RPC — Full Submission)
- **Method**: `educampus.exam.v1.ExamService / SubmitExam`
- **Request Payload**:
```json
{
  "exam_session_id": 0,
  "exam_id": 101,
  "student_id": 42,
  "answers": [
    {
      "question_id": 1,
      "selected_option_id": 2
    },
    {
      "question_id": 2,
      "selected_option_id": 7
    },
    {
      "question_id": 3,
      "text_response": "REST uses HTTP/1.1 and JSON while gRPC uses HTTP/2 with Protocol Buffers for fast binary serialization"
    }
  ]
}
```
- **Response (`ExamSubmissionResponse`)**:
```json
{
  "exam_session_id": 4,
  "total_score": 10.0,
  "max_score": 10.0,
  "grade": "A",
  "passed": true
}
```

---

#### Test A2: `StreamExamProgress` (Server-Streaming RPC)
- **Method**: `educampus.exam.v1.ExamService / StreamExamProgress`
- **Request Payload**:
```json
{
  "exam_session_id": 0,
  "exam_id": 101,
  "student_id": 99,
  "answers": [
    {
      "question_id": 1,
      "selected_option_id": 2
    },
    {
      "question_id": 2,
      "selected_option_id": 5
    },
    {
      "question_id": 3,
      "text_response": "gRPC uses Protocol Buffers and HTTP/2 for efficient binary serialization whereas REST uses JSON"
    }
  ]
}
```
- **Stream Output (`ExamProgressEvent` stream)**:
  - Event 1: `question_id: 1, status: CORRECTING, progress_percentage: 0`
  - Event 2: `question_id: 1, status: CORRECTED, points_earned: 4.0, progress_percentage: 33`
  - Event 3: `question_id: 2, status: CORRECTING, progress_percentage: 33`
  - Event 4: `question_id: 2, status: CORRECTED, points_earned: 0.0, progress_percentage: 67`
  - Event 5: `question_id: 3, status: CORRECTING, progress_percentage: 67`
  - Event 6: `question_id: 3, status: CORRECTED, points_earned: 3.0, progress_percentage: 100, is_final: true`

---

### B. Room Management Testing

#### Test B1: `CreateRoom`
- **Method**: `smartspace.RoomService / CreateRoom`
- **Request Payload**:
```json
{
  "name": "Amphi Turing",
  "capacity": 150,
  "location": "Bâtiment Alan Turing - RDC"
}
```
- **Response**:
```json
{
  "room": {
    "id": 4,
    "name": "Amphi Turing",
    "capacity": 150,
    "location": "Bâtiment Alan Turing - RDC",
    "is_active": true
  }
}
```

#### Test B2: `GetRoom`
- **Method**: `smartspace.RoomService / GetRoom`
- **Request Payload**:
```json
{
  "id": 1
}
```

#### Test B3: `ListRooms`
- **Method**: `smartspace.RoomService / ListRooms`
- **Request Payload**: `{}`
- **Response**: List of all existing rooms including seeded ones (`Amphi A`, `Salle B101`, `Labo Réseau`) and all their confirmed reservations.

#### Test B4: `UpdateRoom`
- **Method**: `smartspace.RoomService / UpdateRoom`
- **Request Payload**:
```json
{
  "id": 1,
  "name": "Amphi A - Rénové",
  "capacity": 120,
  "location": "Bâtiment Principal - RDC Aile Ouest"
}
```

#### Test B5: `DeleteRoom`
- **Method**: `smartspace.RoomService / DeleteRoom`
- **Request Payload**:
```json
{
  "id": 3
}
```
- **Response**:
```json
{
  "success": true
}
```

---

### C. Reservation & Anti-Overlap Testing

#### Test C1: `CreateReservation` (Successful Booking)
- **Method**: `smartspace.ReservationService / CreateReservation`
- **Request Payload**:
```json
{
  "room_id": 1,
  "user": "student_amine",
  "start_time": "2026-11-25T09:00:00",
  "end_time": "2026-11-25T11:00:00",
  "examen_session_id": "session_math_101"
}
```

#### Test C2: `GetReservation`
- **Method**: `smartspace.ReservationService / GetReservation`
- **Request Payload**:
```json
{
  "id": 1
}
```

#### Test C3: `ListReservations` (Optionally Filtered by Room)
- **Method**: `smartspace.ReservationService / ListReservations`
- **Request Payload**:
```json
{
  "room_id": 1
}
```
*(Leave `"room_id": 0` to list all reservations across all rooms)*

#### Test C4: `DeleteReservation`
- **Method**: `smartspace.ReservationService / DeleteReservation`
- **Request Payload**:
```json
{
  "id": 1
}
```
- **Response**:
```json
{
  "success": true
}
```

#### Test C5: Anti-Overlap Rule Trigger (Conflict Verification)
- Send an overlapping reservation for `room_id: 1`:
```json
{
  "room_id": 1,
  "user": "student_another",
  "start_time": "2026-11-25T10:00:00",
  "end_time": "2026-11-25T12:00:00",
  "examen_session_id": "session_algo_202"
}
```
- **Result**:
  - **Status**: `10 ABORTED`
  - **Error Description**: `"Reservation time overlaps with an existing reservation"`

---

## 🛡️ Exception & Status Code Mapping Reference

| Condition / Scenario | Exception Class | gRPC Status Code | Description / Error Message |
|---|---|---|---|
| Non-positive Student ID / Exam ID / Question ID | `InvalidExamSubmissionException` | `INVALID_ARGUMENT` (3) | `"Student ID must be a positive number"` |
| Empty Answers list | `InvalidExamSubmissionException` | `INVALID_ARGUMENT` (3) | `"At least one answer is required"` |
| Submitting already completed session | `InvalidExamSubmissionException` | `INVALID_ARGUMENT` (3) | `"Exam session <id> has already been submitted and completed. Re-submission is not allowed."` |
| ExamSession or Question ID not found | `ExamSessionNotFoundException` / `QuestionNotFoundException` | `NOT_FOUND` (5) | `"ExamSession not found: id=<id>"` |
| Reservation time overlap | `ReservationOverlapException` | `ABORTED` (10) | `"Reservation time overlaps with an existing reservation"` |
| Reservation start >= end | `IllegalArgumentException` | `INVALID_ARGUMENT` (3) | `"Start time must be before end time"` |
| Database constraint violation | `DataIntegrityViolationException` | `ALREADY_EXISTS` (6) | `"Conflict with existing database records: ..."` |
| Unhandled server error | `Exception` | `INTERNAL` (13) | `"Internal error: ..."` |

---

## 🧪 Running Automated Tests

Run the complete test suite (51 tests):
```bash
cd gRPC
./mvnw clean test
```
All unit tests and Spring Boot integration tests will execute using in-memory H2 (PostgreSQL compatibility mode).
