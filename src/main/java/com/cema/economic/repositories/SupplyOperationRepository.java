package com.cema.economic.repositories;

import com.cema.economic.entities.CemaBovineOperation;
import com.cema.economic.entities.CemaSupplyOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupplyOperationRepository extends JpaRepository<CemaSupplyOperation, UUID> {

    CemaSupplyOperation findCemaSupplyOperationById(UUID id);

    CemaSupplyOperation findCemaSupplyOperationByIdAndEstablishmentCuigIgnoreCase(UUID id, String cuig);

    Page<CemaSupplyOperation> findAllByEstablishmentCuig(String cuig, Pageable paging);

    List<CemaSupplyOperation> findAllByEstablishmentCuigAndCemaSupplyName(String cuig, String supplyName);
}
