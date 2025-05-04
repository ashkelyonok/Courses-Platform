package com.askel.coursesplatform.repository;

import com.askel.coursesplatform.model.entity.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByName(String name);

    @Query("SELECT c FROM Course c JOIN c.students s WHERE s.id = :studentId")
    List<Course> findByStudentId(@Param("studentId") Long studentId);

    @Query(value = "SELECT c.* FROM courses c "
            + "JOIN course_students cs ON c.id = cs.course_id "
            + "JOIN users u ON cs.user_id = u.id "
            + "WHERE u.name = :studentName", nativeQuery = true)
    List<Course> findAllByStudentName(@Param("studentName") String studentName);

    List<Course> findByInstructorId(Long instructorId);

    @Query("SELECT DISTINCT c FROM Course c JOIN FETCH c.instructor i WHERE i.name = :instructorName")
    List<Course> findByInstructorName(@Param("instructorName") String instructorName);

    boolean existsByName(String name);
}
