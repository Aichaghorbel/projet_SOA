# Diagramme de classes — Academic Service

Diagramme Mermaid (à visualiser sur GitHub/GitLab, ou sur https://mermaid.live
puis exporter en PNG/SVG pour le rapport).

```mermaid
classDiagram
    direction LR

    class Student {
        +Long id
        +String studentNumber
        +String firstName
        +String lastName
        +String email
        +LocalDate dateOfBirth
        +String phone
        +String program
        +Integer level
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class Course {
        +Long id
        +String code
        +String title
        +String description
        +Integer credits
        +Semester semester
        +String teacherName
        +Integer capacity
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class CourseSchedule {
        +Long id
        +int dayOfWeek
        +LocalTime startTime
        +LocalTime endTime
        +Long roomId
    }

    class Enrollment {
        +Long id
        +String academicYear
        +EnrollmentStatus status
        +LocalDateTime enrolledAt
        +LocalDateTime updatedAt
    }

    class Exam {
        +Long id
        +String title
        +LocalDateTime examDate
        +int durationMin
        +Long roomId
        +BigDecimal maxGrade
    }

    class Result {
        +Long id
        +BigDecimal grade
        +SessionType sessionType
        +ResultStatus status
        +ResultSource source
        +String comment
        +LocalDateTime createdAt
        +LocalDateTime publishedAt
    }

    class EnrollmentStatus {
        <<enumeration>>
        PENDING
        CONFIRMED
        CANCELLED
        COMPLETED
    }

    class ResultStatus {
        <<enumeration>>
        DRAFT
        PUBLISHED
    }

    class SessionType {
        <<enumeration>>
        NORMAL
        RATTRAPAGE
    }

    class Semester {
        <<enumeration>>
        S1
        S2
        S3
        S4
        S5
        S6
    }

    class ResultSource {
        <<enumeration>>
        MANUAL
        EXAM_AUTO
    }

    Student "1" --> "0..*" Enrollment : s'inscrit
    Course "1" --> "0..*" Enrollment : reçoit
    Course "1" --> "0..*" CourseSchedule : planifié
    Course "1" --> "0..*" Exam : évalué par
    Enrollment "1" --> "0..*" Result : produit
    Exam "1" --> "0..*" Result : donne lieu à
    Course --> Semester
    Enrollment --> EnrollmentStatus
    Result --> ResultStatus
    Result --> SessionType
    Result --> ResultSource
```

## Règles métier portées par le modèle

- Un étudiant ne peut s'inscrire qu'une fois à un cours pour une année
  académique donnée (`UNIQUE (student_id, course_id, academic_year)`).
- Une note est comprise entre 0 et 20.
- Un résultat est unique par (inscription, examen, type de session).
- `Exam.roomId` et `CourseSchedule.roomId` sont des **références logiques**
  vers le service Smart Space (pas de clé étrangère entre services).
