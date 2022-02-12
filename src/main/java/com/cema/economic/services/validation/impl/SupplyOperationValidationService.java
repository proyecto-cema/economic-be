package com.cema.economic.services.validation.impl;

import com.cema.economic.constants.OperationType;
import com.cema.economic.domain.SupplyOperation;
import com.cema.economic.exceptions.ValidationException;
import com.cema.economic.services.database.DatabaseService;
import com.cema.economic.services.validation.OperationValidationService;
import org.springframework.stereotype.Service;

@Service
public class SupplyOperationValidationService implements OperationValidationService<SupplyOperation> {

    private final DatabaseService databaseService;

    public SupplyOperationValidationService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void validateOperation(SupplyOperation operation) {
        if(OperationType.LOSS.equalsIgnoreCase(operation.getOperationType()) || OperationType.USE.equalsIgnoreCase(operation.getOperationType())){
            String supplyName = operation.getSupplyName();
            String cuig = operation.getEstablishmentCuig();
            long cost = operation.getAmount();

            int available = databaseService.getAvailableSupplyByName(supplyName, cuig);
            if(available < cost){
                throw new ValidationException(
                        String.format("This operation amount %d exceeds what is available %d for the supply %s", cost, available, supplyName));
            }
        }
    }
}
