package com.askel.coursesplatform.repository;

import com.askel.coursesplatform.model.entity.Course;
import java.util.List;
import java.util.Optional;
import com.askel.coursesplatform.model.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByName(String name);

    @Query("SELECT c FROM Course c JOIN FETCH c.students s JOIN FETCH c.instructor WHERE s.id = :studentId")
    List<Course> findByStudentId(@Param("studentId") Long studentId);

//    @Query(value = "SELECT c.* FROM courses c "
//            + "JOIN course_students cs ON c.id = cs.course_id "
//            + "JOIN users u ON cs.user_id = u.id "
//            + "WHERE u.name = :studentName", nativeQuery = true)
    @Query("SELECT c FROM Course c JOIN FETCH c.instructor LEFT JOIN FETCH c.students s WHERE s.name = :studentName")
    List<Course> findAllByStudentName(@Param("studentName") String studentName);

    @Query("SELECT c FROM Course c JOIN FETCH c.instructor i LEFT JOIN FETCH c.students WHERE i.id = :instructorId")
    List<Course> findByInstructorId(Long instructorId);

    @Query("SELECT DISTINCT c FROM Course c JOIN FETCH c.instructor i WHERE i.name = :instructorName")
    List<Course> findByInstructorName(@Param("instructorName") String instructorName);

    boolean existsByName(String name);

    List<Course> findByStatus(CourseStatus status);
    List<Course> findByNameContainingIgnoreCase(String namePart);
    List<Course> findByDescriptionContainingIgnoreCase(String descriptionPart);
    List<Course> findByStatusAndNameContainingIgnoreCase(CourseStatus status, String namePart);
    List<Course> findByStudentsEmpty();
    List<Course> findByInstructorIsNull();

    @Query("SELECT c FROM Course c WHERE c.instructor IS NOT NULL")
    List<Course> findWithInstructor();

    @Query("SELECT c FROM Course c WHERE SIZE(c.students) > 0")
    List<Course> findWithStudents();

    boolean existsByIdAndInstructorId(Long courseId, Long instructorId);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.instructor LEFT JOIN FETCH c.students WHERE c.id = :id")
    Optional<Course> findByIdWithDetails(@Param("id") Long id);
}
