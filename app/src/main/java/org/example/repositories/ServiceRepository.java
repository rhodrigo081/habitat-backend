package org.example.repositories;

import org.example.models.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByAssociateId(Long associateId);

    Page<Service> findByAssociateId(Long associateId, Pageable pageable);

    List<Service> findByInternId(Long internId);

    Page<Service> findByInternId(Long internId, Pageable pageable);

    @Query("""
        SELECT at FROM Service at
        WHERE at.intern.coordinator.id = :coordinatorId
        ORDER BY at.date DESC
        """)
    Page<Service> findByCoordenadorId(@Param("coordinatorId") Long coordinatorId, Pageable pageable);

    @Query("""
        SELECT at FROM Service at
        WHERE at.intern.coordinator.id = :coordinatorId
        AND at.date BETWEEN :dateStart AND :dateEnd
        ORDER BY at.date DESC
        """)
    List<Service> findByCoordenadorIdEPeriodo(
            @Param("coordinatorId") Long coordinatorId,
            @Param("dateStart") LocalDate dateStart,
            @Param("dateEnd") LocalDate dateEnd);

    long countByInternId(Long internId);

    @Query("SELECT COUNT(at) FROM Service at WHERE at.intern.coordinator.id = :coordinatorId")
    long countByCoordinatorId(@Param("coordinatorId") Long coordinatorId);
}