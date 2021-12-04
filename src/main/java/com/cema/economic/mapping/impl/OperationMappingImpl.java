package com.cema.economic.mapping.impl;

import com.cema.economic.domain.Operation;
import com.cema.economic.entities.CemaOperation;
import com.cema.economic.mapping.OperationMapping;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class OperationMappingImpl implements OperationMapping {

    @Override
    public Operation mapEntityToDomain(CemaOperation cemaOperation) {
        return Operation.builder()
                .bovineTag(cemaOperation.getBovineTag())
                .operationType(cemaOperation.getOperationType())
                .buyerName(cemaOperation.getBuyerName())
                .sellerName(cemaOperation.getSellerName())
                .id(cemaOperation.getId())
                .transactionDate(cemaOperation.getTransactionDate())
                .amount(cemaOperation.getAmount())
                .description(cemaOperation.getDescription())
                .establishmentCuig(cemaOperation.getEstablishmentCuig())
                .operatorUserName(cemaOperation.getOperatorName())
                .build();
    }

    @Override
    public CemaOperation mapDomainToEntity(Operation operation) {
        return CemaOperation.builder()
                .bovineTag(operation.getBovineTag())
                .operationType(operation.getOperationType())
                .buyerName(operation.getBuyerName())
                .sellerName(operation.getSellerName())
                .transactionDate(operation.getTransactionDate())
                .amount(operation.getAmount())
                .description(operation.getDescription())
                .establishmentCuig(operation.getEstablishmentCuig())
                .operatorName(operation.getOperatorUserName())
                .build();
    }

    @Override
    public CemaOperation updateDomainWithEntity(Operation operation, CemaOperation cemaOperation) {
        String description = StringUtils.hasText(operation.getDescription()) ? operation.getDescription() : cemaOperation.getDescription();
        Long amount = operation.getAmount() != null ? operation.getAmount() : cemaOperation.getAmount();
        String seller = StringUtils.hasText(operation.getSellerName()) ? operation.getSellerName() : cemaOperation.getSellerName();
        String buyer = StringUtils.hasText(operation.getBuyerName()) ? operation.getBuyerName() : cemaOperation.getBuyerName();
        Date transactionDate = operation.getTransactionDate() != null ? operation.getTransactionDate() : cemaOperation.getTransactionDate();

        cemaOperation.setDescription(description);
        cemaOperation.setAmount(amount);
        cemaOperation.setSellerName(seller);
        cemaOperation.setBuyerName(buyer);
        cemaOperation.setTransactionDate(transactionDate);
        return cemaOperation;
    }
}
