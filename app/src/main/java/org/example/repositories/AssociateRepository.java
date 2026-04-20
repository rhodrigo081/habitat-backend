package org.example.repositories;

import org.example.models.Associate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssociateRepository extends JpaRepository<Associate, Long> {

    Optional<Associate> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Associate> findByInternId(Long internId);

    Page<Associate> findByInternId(Long internId, Pageable pageable);

    @Query("""
        SELECT a FROM Associate a
        WHERE a.intern.coordinator.id = :coordenadorId
        ORDER BY a.createdAt DESC
        """)
    List<Associate> findByCoordinatorId(@Param("coordinatorId") Long coordinatorId);

    @Query("""
        SELECT a FROM Associate a
        WHERE a.intern.coordinator.id = :coordinatorId
        ORDER BY a.createdAt DESC
        """)
    Page<Associate> findByCoordinatorId(@Param("coordinatorId") Long coordinatorId, Pageable pageable);

    @Query("""
        SELECT a FROM Associate a
        WHERE (LOWER(a.name) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR a.cpf LIKE CONCAT('%', :termo, '%'))
        AND a.intern.coordinator.id = :coordenadorId
        """)
    Page<Associate> findByTermAndCoordinator(
            @Param("term") String termo,
            @Param("coordinatorId") Long coordinatorId,
            Pageable pageable);

    @Query("""
        SELECT a FROM Associate a
        WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR a.cpf LIKE CONCAT('%', :termo, '%')
        """)
    Page<Associate> findByTerm(@Param("term") String term, Pageable pageable);
}
