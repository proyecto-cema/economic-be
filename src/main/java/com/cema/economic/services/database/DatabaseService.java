package com.cema.economic.services.database;

import com.cema.economic.entities.CemaSupply;
import org.springframework.data.domain.Page;

public interface DatabaseService {
    CemaSupply saveCemaSupply(CemaSupply cemaSupply, String categoryName);

    Page<CemaSupply> searchSupplies(CemaSupply cemaSupply, String category, int page, int size);
}
