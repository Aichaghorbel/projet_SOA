package com.educampus.academic.repository;

import com.educampus.academic.entity.Course;
import com.educampus.academic.entity.Semester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Contrôles de doublon (POST -> 409) et (PUT : on exclut le cours modifié)
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    /** Filtre par semestre seul (paramètre semester). */
    Page<Course> findBySemester(Semester semester, Pageable pageable);

    /** Recherche texte seule (paramètre q) : q doit être non null, passer "" pour tout lister. */
    @Query("SELECT c FROM Course c WHERE "
            + "LOWER(c.code) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(c.teacherName) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Course> search(@Param("q") String q, Pageable pageable);

    /** Filtre semestre + recherche texte (les deux paramètres fournis). */
    @Query("SELECT c FROM Course c WHERE c.semester = :semester AND ("
            + "LOWER(c.code) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(c.teacherName) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Course> searchBySemester(@Param("semester") Semester semester,
                                  @Param("q") String q, Pageable pageable);
}
