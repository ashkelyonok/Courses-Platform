package com.askel.coursesplatform.repository;

import com.askel.coursesplatform.model.entity.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByName(String name);

    @Query("SELECT c FROM Course c JOIN FETCH c.students s WHERE s.id = :studentId")
    List<Course> findByStudentId(@Param("studentId") Long studentId);

    List<Course> findByInstructorId(Long instructorId);
}
