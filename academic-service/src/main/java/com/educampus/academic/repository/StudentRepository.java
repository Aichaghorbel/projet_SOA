package com.educampus.academic.repository;

import com.educampus.academic.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Contrôles de doublon (POST -> 409) et (PUT : on exclut l'étudiant modifié)
    boolean existsByEmail(String email);

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByStudentNumberAndIdNot(String studentNumber, Long id);

    /** Recherche texte (paramètre q) : q doit être non null, passer "" pour tout lister. */
    @Query("SELECT s FROM Student s WHERE "
            + "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(s.email) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(s.studentNumber) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Student> search(@Param("q") String q, Pageable pageable);
}
