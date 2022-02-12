package com.cema.economic.repositories;

import com.cema.economic.entities.CemaSupply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyRepository extends JpaRepository<CemaSupply, String> {

    CemaSupply findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(String name, String cuig);

    CemaSupply findCemaSupplyByNameIgnoreCase(String name);

    Page<CemaSupply> findAllByEstablishmentCuig(String cuig, Pageable paging);

    @Query("select sup.name from CemaSupply sup where sup.establishmentCuig =?1")
    List<String> findAllAvailableSupplies(String cuig);
}
