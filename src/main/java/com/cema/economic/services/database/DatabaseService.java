package com.cema.economic.services.database;

import com.cema.economic.entities.CemaSupply;
import com.cema.economic.entities.CemaSupplyOperation;
import org.springframework.data.domain.Page;

public interface DatabaseService {
    int getAvailableSupplyByName(String name, String cuig);

    CemaSupplyOperation saveSupplyOperation(CemaSupplyOperation cemaSupplyOperation, String supplyName);

    CemaSupply saveCemaSupply(CemaSupply cemaSupply, String categoryName);

    Page<CemaSupply> searchSupplies(CemaSupply cemaSupply, String category, int page, int size);

    boolean canBeDeleted(String supplyName, String cuig);
}
