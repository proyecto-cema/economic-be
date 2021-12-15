package com.cema.economic.repositories;

import com.cema.economic.entities.CemaOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OperationRepository extends JpaRepository<CemaOperation, UUID> {

    CemaOperation findCemaOperationById(UUID id);

    CemaOperation findCemaOperationByIdAndEstablishmentCuigIgnoreCase(UUID id, String cuig);

    Page<CemaOperation> findAllByEstablishmentCuig(String cuig, Pageable paging);
}
