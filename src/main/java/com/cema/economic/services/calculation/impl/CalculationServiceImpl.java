package com.cema.economic.services.calculation.impl;

import com.cema.economic.constants.OperationType;
import com.cema.economic.entities.CemaSupplyOperation;
import com.cema.economic.services.calculation.CalculationService;
import org.springframework.stereotype.Service;

@Service
public class CalculationServiceImpl implements CalculationService {

    @Override
    public long getSignedAmount(CemaSupplyOperation cemaSupplyOperation) {
        String type = cemaSupplyOperation.getOperationType();
        if (OperationType.BUY.equals(type)) {
            return cemaSupplyOperation.getAmount();
        } else if (OperationType.USE.equals(type) || OperationType.LOSS.equals(type)) {
            return -cemaSupplyOperation.getAmount();
        }
        return 0;
    }
}
