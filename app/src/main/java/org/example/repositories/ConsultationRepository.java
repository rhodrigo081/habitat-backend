package org.example.repositories;

import org.example.models.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByAssociateId(Long associateId);

    Page<Consultation> findByAssociateId(Long associateId, Pageable pageable);

    List<Consultation> findByInternId(Long internId);

    Page<Consultation> findByInternId(Long internId, Pageable pageable);

    @Query("""
        SELECT at FROM Consultation at
        WHERE at.intern.coordinator.id = :coordinatorId
        ORDER BY at.date DESC
        """)
    Page<Consultation> findByCoordinatorId(@Param("coordinatorId") Long coordinatorId, Pageable pageable);

    @Query("""
        SELECT at FROM Consultation at
        WHERE at.intern.coordinator.id = :coordinatorId
        AND at.date BETWEEN :dateStart AND :dateEnd
        ORDER BY at.date DESC
        """)
    List<Consultation> findByCoordinatorIdEPeriodo(
            @Param("coordinatorId") Long coordinatorId,
            @Param("dateStart") LocalDate dateStart,
            @Param("dateEnd") LocalDate dateEnd);

    long countByInternId(Long internId);

    @Query("SELECT COUNT(at) FROM Consultation at WHERE at.intern.coordinator.id = :coordinatorId")
    long countByCoordinatorId(@Param("coordinatorId") Long coordinatorId);
}