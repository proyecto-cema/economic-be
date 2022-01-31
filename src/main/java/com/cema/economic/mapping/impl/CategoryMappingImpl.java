package com.cema.economic.mapping.impl;

import com.cema.economic.domain.Category;
import com.cema.economic.entities.CemaCategory;
import com.cema.economic.mapping.Mapping;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CategoryMappingImpl implements Mapping<CemaCategory, Category> {

    @Override
    public Category mapEntityToDomain(CemaCategory entity) {
        return Category.builder()
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    @Override
    public CemaCategory mapDomainToEntity(Category domain) {
        return CemaCategory.builder()
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }

    @Override
    public CemaCategory updateDomainWithEntity(Category domain, CemaCategory entity) {
        String description = StringUtils.hasText(domain.getDescription()) ? domain.getDescription() : entity.getDescription();
        entity.setDescription(description);
        return entity;
    }
}
