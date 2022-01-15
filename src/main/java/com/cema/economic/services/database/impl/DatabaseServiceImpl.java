package com.cema.economic.services.database.impl;

import com.cema.economic.entities.CemaCategory;
import com.cema.economic.entities.CemaSupply;
import com.cema.economic.exceptions.NotFoundException;
import com.cema.economic.repositories.CategoryRepository;
import com.cema.economic.repositories.SupplyRepository;
import com.cema.economic.services.database.DatabaseService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DatabaseServiceImpl implements DatabaseService {

    private final CategoryRepository categoryRepository;
    private final SupplyRepository supplyRepository;

    public DatabaseServiceImpl(CategoryRepository categoryRepository, SupplyRepository supplyRepository) {
        this.categoryRepository = categoryRepository;
        this.supplyRepository = supplyRepository;
    }

    @Override
    public CemaSupply saveCemaSupply(CemaSupply cemaSupply, String categoryName){
        Optional<CemaCategory> cemaCategoryOptional = categoryRepository.findById(categoryName);
        if(!cemaCategoryOptional.isPresent()){
            throw new NotFoundException(String.format("The category %s does not exists", categoryName));
        }
        cemaSupply.setCategory(cemaCategoryOptional.get());

        return supplyRepository.save(cemaSupply);
    }
}
