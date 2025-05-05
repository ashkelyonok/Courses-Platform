package com.askel.coursesplatform.repository;

import com.askel.coursesplatform.model.entity.User;
import java.util.List;
import java.util.Optional;
import com.askel.coursesplatform.model.enums.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByName(String name);

    Optional<User> findByName(String name);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRoles role);

    List<User> findByEnrolledCoursesId(Long courseId);

    List<User> findByTaughtCoursesId(Long courseId);

    List<User> findByNameContainingIgnoreCase(String namePart);

    List<User> findByRoleAndNameContainingIgnoreCase(UserRoles role, String namePart);
}
