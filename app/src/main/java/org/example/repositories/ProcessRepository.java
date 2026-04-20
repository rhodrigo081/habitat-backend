package org.example.repositories;

import org.example.enums.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.example.models.Process;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Long> {

    Optional<Process> findByProcessNumber(String processNumber);

    boolean existsByProcessNumber(String processNumber);

    List<Process> findByAssociateId(Long associateId);

    Page<Process> findByAssociateId(Long associateId, Pageable pageable);

    List<Process> findByInternId(Long internId);

    List<Process> findByCurrentStatus(ProcessStatus status);

    @Query("""
        SELECT p FROM Process p
        WHERE p.intern.coordinator.id = :coordinatorId
        ORDER BY p.createdAt DESC
        """)
    Page<Process> findByCoordinatorId(@Param("coordinatorId") Long coordinatorId, Pageable pageable);

    @Query("""
        SELECT p FROM Process p
        WHERE p.intern.coordinator.id = :coordinatorId
        AND p.currentStatus = :fase
        """)
    List<Process> findByCoordinatorIdAndCurrentStatus(
            @Param("coordinatorId") Long coordinatorId,
            @Param("currentStatus") ProcessStatus currentStatus);

    @Query("""
        SELECT p FROM Process p
        WHERE LOWER(p.processNumber) LIKE LOWER(CONCAT('%', :term, '%'))
            OR LOWER(p.city) LIKE LOWER(CONCAT('%', :term, '%'))
            AND p.intern.coordinator.id = :coordinatorId
        """)
    Page<Process> findByTermAndCoordinator(
            @Param("term") String term,
            @Param("coordinatorId") Long coordinatorId,
            Pageable pageable);

    long countByInternId(Long internId);
}
