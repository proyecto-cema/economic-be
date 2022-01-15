package com.cema.economic.mapping.impl;

import com.cema.economic.domain.Supply;
import com.cema.economic.entities.CemaSupply;
import com.cema.economic.mapping.Mapping;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SupplyMappingImpl implements Mapping<CemaSupply, Supply> {

    @Override
    public Supply mapEntityToDomain(CemaSupply entity) {
        return Supply.builder()
                .name(entity.getName())
                .price(entity.getPrice())
                .units(entity.getUnits())
                .categoryName(entity.getCategory().getName())
                .establishmentCuig(entity.getEstablishmentCuig())
                .build();
    }

    @Override
    public CemaSupply mapDomainToEntity(Supply domain) {
        return CemaSupply.builder()
                .name(domain.getName())
                .price(domain.getPrice())
                .units(domain.getUnits())
                .establishmentCuig(domain.getEstablishmentCuig())
                .build();
    }

    @Override
    public CemaSupply updateDomainWithEntity(Supply domain, CemaSupply entity) {
        Long price = domain.getPrice() != null ? domain.getPrice() : entity.getPrice();
        String units = StringUtils.hasText(domain.getUnits()) ? domain.getUnits() : entity.getUnits();

        entity.setPrice(price);
        entity.setUnits(units);
        return entity;
    }
}
