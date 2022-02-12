package com.cema.economic.mapping.impl;

import com.cema.economic.domain.BovineOperation;
import com.cema.economic.domain.SupplyOperation;
import com.cema.economic.entities.CemaBovineOperation;
import com.cema.economic.entities.CemaSupplyOperation;
import com.cema.economic.mapping.Mapping;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class SupplyOperationMappingImpl implements Mapping<CemaSupplyOperation, SupplyOperation> {

    @Override
    public SupplyOperation mapEntityToDomain(CemaSupplyOperation entity) {
        return SupplyOperation.builder()
                .operationType(entity.getOperationType())
                .supplyName(entity.getCemaSupply().getName())
                .id(entity.getId())
                .transactionDate(entity.getTransactionDate())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .establishmentCuig(entity.getEstablishmentCuig())
                .operatorUserName(entity.getOperatorName())
                .build();
    }

    @Override
    public CemaSupplyOperation mapDomainToEntity(SupplyOperation domain) {
        return CemaSupplyOperation.builder()
                .operationType(domain.getOperationType())
                .transactionDate(domain.getTransactionDate())
                .amount(domain.getAmount())
                .description(domain.getDescription())
                .establishmentCuig(domain.getEstablishmentCuig())
                .operatorName(domain.getOperatorUserName())
                .build();
    }

    @Override
    public CemaSupplyOperation updateDomainWithEntity(SupplyOperation domain, CemaSupplyOperation entity) {
        String description = StringUtils.hasText(domain.getDescription()) ? domain.getDescription() : entity.getDescription();
        Date transactionDate = domain.getTransactionDate() != null ? domain.getTransactionDate() : entity.getTransactionDate();

        entity.setDescription(description);
        entity.setTransactionDate(transactionDate);
        return entity;
    }
}
