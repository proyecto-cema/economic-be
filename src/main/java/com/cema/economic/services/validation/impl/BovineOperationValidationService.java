package com.cema.economic.services.validation.impl;

import com.cema.economic.domain.BovineOperation;
import com.cema.economic.exceptions.ValidationException;
import com.cema.economic.services.validation.OperationValidationService;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class BovineOperationValidationService implements OperationValidationService<BovineOperation> {

    @Override
    public void validateOperation(BovineOperation bovineOperation) {
        Long amount = bovineOperation.getAmount();
        if (amount != null && amount < 0) {
            throw new ValidationException("Amount cannot be negative");
        }

        if (StringUtils.isEmpty(bovineOperation.getBuyerName()) == StringUtils.isEmpty(bovineOperation.getSellerName())) {
            throw new ValidationException("Buyer or Seller names are required, but not both.");
        }
    }
}
