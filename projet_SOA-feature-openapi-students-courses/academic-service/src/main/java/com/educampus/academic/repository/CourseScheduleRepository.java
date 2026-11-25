package com.educampus.academic.repository;

import com.educampus.academic.entity.CourseSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {

    List<CourseSchedule> findByCourseIdOrderByDayOfWeekAscStartTimeAsc(Long courseId);
}