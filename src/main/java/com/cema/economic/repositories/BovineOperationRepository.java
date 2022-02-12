package com.cema.economic.repositories;

import com.cema.economic.entities.CemaBovineOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BovineOperationRepository extends JpaRepository<CemaBovineOperation, UUID> {

    CemaBovineOperation findCemaBovineOperationById(UUID id);

    CemaBovineOperation findCemaBovineOperationByIdAndEstablishmentCuigIgnoreCase(UUID id, String cuig);

    Page<CemaBovineOperation> findAllByEstablishmentCuig(String cuig, Pageable paging);
}
