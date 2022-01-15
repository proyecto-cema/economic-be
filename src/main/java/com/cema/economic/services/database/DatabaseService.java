package com.cema.economic.services.database;

import com.cema.economic.entities.CemaSupply;

public interface DatabaseService {
    CemaSupply saveCemaSupply(CemaSupply cemaSupply, String categoryName);
}
