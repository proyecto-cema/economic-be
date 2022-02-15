package com.cema.economic.services.calculation;

import com.cema.economic.entities.CemaSupplyOperation;

public interface CalculationService {
    long getSignedAmount(CemaSupplyOperation cemaSupplyOperation);
}
