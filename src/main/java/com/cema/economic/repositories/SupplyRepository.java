package com.cema.economic.repositories;

import com.cema.economic.entities.CemaOperation;
import com.cema.economic.entities.CemaSupply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyRepository extends JpaRepository<CemaSupply, String> {

    CemaSupply findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(String name, String cuig);

    CemaSupply findCemaSupplyByNameIgnoreCase(String name);

    Page<CemaSupply> findAllByEstablishmentCuig(String cuig, Pageable paging);
}
