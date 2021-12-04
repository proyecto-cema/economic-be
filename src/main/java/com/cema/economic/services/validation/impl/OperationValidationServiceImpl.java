package com.cema.economic.services.validation.impl;

import com.cema.economic.domain.Operation;
import com.cema.economic.exceptions.ValidationException;
import com.cema.economic.services.validation.OperationValidationService;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class OperationValidationServiceImpl implements OperationValidationService {

    @Override
    public void validateOperation(Operation operation) {
        Long amount = operation.getAmount();
        if(amount != null && amount < 0){
            throw new ValidationException("Amount cannot be negative");
        }

        if (StringUtils.isEmpty(operation.getBuyerName()) == StringUtils.isEmpty(operation.getSellerName())) {
            throw new ValidationException("Buyer or Seller names are required, but not both.");
        }

        //TODO Implement validation with subscription
    }
}
