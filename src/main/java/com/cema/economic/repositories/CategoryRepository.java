package com.cema.economic.repositories;

import com.cema.economic.entities.CemaCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CemaCategory, String> {

    CemaCategory findCemaCategoryByNameIgnoreCase(String name);
}
