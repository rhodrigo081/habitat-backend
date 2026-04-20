package org.example.repositories;

import org.example.enums.UserRole;
import org.example.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByCoordinatorId(Long coordinatorId);

    List<User> findByRoleAndStatusTrue(UserRole role);

    @Query("SELECT u FROM User u WHERE u.coordinator.id = :coordenadorId AND u.status = true")
    List<User> findInternsByCoordinatorId(@Param("coordinatorId") Long coordinatorId);

    @Query("SELECT u FROM User u WHERE u.role = 'COORDENADOR' AND u.status = true")
    List<User> findAllCoordinatorsTrue();
}
