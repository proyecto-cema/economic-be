package com.cema.economic.mapping.impl;

import com.cema.economic.domain.BovineOperation;
import com.cema.economic.entities.CemaBovineOperation;
import com.cema.economic.mapping.Mapping;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class BovineOperationMappingImpl implements Mapping<CemaBovineOperation, BovineOperation> {

    @Override
    public BovineOperation mapEntityToDomain(CemaBovineOperation entity) {
        return BovineOperation.builder()
                .bovineTag(entity.getBovineTag())
                .operationType(entity.getOperationType())
                .buyerName(entity.getBuyerName())
                .sellerName(entity.getSellerName())
                .id(entity.getId())
                .transactionDate(entity.getTransactionDate())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .establishmentCuig(entity.getEstablishmentCuig())
                .operatorUserName(entity.getOperatorName())
                .build();
    }

    @Override
    public CemaBovineOperation mapDomainToEntity(BovineOperation domain) {
        return CemaBovineOperation.builder()
                .bovineTag(domain.getBovineTag())
                .operationType(domain.getOperationType())
                .buyerName(domain.getBuyerName())
                .sellerName(domain.getSellerName())
                .transactionDate(domain.getTransactionDate())
                .amount(domain.getAmount())
                .description(domain.getDescription())
                .establishmentCuig(domain.getEstablishmentCuig())
                .operatorName(domain.getOperatorUserName())
                .build();
    }

    @Override
    public CemaBovineOperation updateDomainWithEntity(BovineOperation domain, CemaBovineOperation entity) {
        String description = StringUtils.hasText(domain.getDescription()) ? domain.getDescription() : entity.getDescription();
        Long amount = domain.getAmount() != null ? domain.getAmount() : entity.getAmount();
        String seller = StringUtils.hasText(domain.getSellerName()) ? domain.getSellerName() : entity.getSellerName();
        String buyer = StringUtils.hasText(domain.getBuyerName()) ? domain.getBuyerName() : entity.getBuyerName();
        Date transactionDate = domain.getTransactionDate() != null ? domain.getTransactionDate() : entity.getTransactionDate();

        entity.setDescription(description);
        entity.setAmount(amount);
        entity.setSellerName(seller);
        entity.setBuyerName(buyer);
        entity.setTransactionDate(transactionDate);
        return entity;
    }
}
