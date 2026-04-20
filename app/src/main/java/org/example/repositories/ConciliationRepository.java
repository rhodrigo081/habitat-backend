package org.example.repositories;

import org.example.enums.CitationStatus;
import org.example.models.Conciliation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConciliationRepository extends JpaRepository<Conciliation, Long> {

    List<Conciliation> findByAssociateId(Long associateId);

    Page<Conciliation> findByAssociateId(Long associateId, Pageable pageable);

    List<Conciliation> findByInternId(Long internId);

    List<Conciliation> findByCitationStatus(CitationStatus status);

    @Query("""
        SELECT c FROM Conciliation c
        WHERE c.intern.coordinator.id = :coordinatorId
        ORDER BY c.audienceDateTime ASC
        """)
    Page<Conciliation> findByCoordinatorId(@Param("coordinatorId") Long coordinatorId, Pageable pageable);

    @Query("""
        SELECT c FROM Conciliation c
        WHERE c.audienceDateTime BETWEEN :start AND :end
        AND c.intern.coordinator.id = :coordinatorId
        ORDER BY c.audienceDateTime ASC
        """)
    List<Conciliation> findAudienciasScheduled(
            @Param("coordinatorId") Long coordinatorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
        SELECT c FROM Conciliation c
        WHERE c.audienceDateTime BETWEEN :start AND :end
        ORDER BY c.audienceDateTime ASC
        """)
    List<Conciliation> findAudiencesDuringThePeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    long countByInternId(Long internId);
}