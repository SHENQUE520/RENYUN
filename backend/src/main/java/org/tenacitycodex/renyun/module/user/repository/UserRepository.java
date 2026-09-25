package org.tenacitycodex.renyun.module.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tenacitycodex.renyun.module.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserById(Long id);
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String username);

    List<User> findByRoleAndStatusOrderByCreatedAtAsc(String role, String status);

    List<User> findByRole(String role);

    Optional<User> findByUsername(String username);
}
