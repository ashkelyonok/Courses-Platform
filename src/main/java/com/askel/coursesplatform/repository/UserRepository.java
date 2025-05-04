package com.askel.coursesplatform.repository;

import com.askel.coursesplatform.model.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByName(String name);

    Optional<User> findByName(String name);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
