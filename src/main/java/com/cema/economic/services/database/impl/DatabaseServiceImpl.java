package com.cema.economic.services.database.impl;

import com.cema.economic.entities.CemaCategory;
import com.cema.economic.entities.CemaSupply;
import com.cema.economic.entities.CemaSupplyOperation;
import com.cema.economic.exceptions.NotFoundException;
import com.cema.economic.repositories.CategoryRepository;
import com.cema.economic.repositories.SupplyOperationRepository;
import com.cema.economic.repositories.SupplyRepository;
import com.cema.economic.services.calculation.CalculationService;
import com.cema.economic.services.database.DatabaseService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class DatabaseServiceImpl implements DatabaseService {

    private final CategoryRepository categoryRepository;
    private final SupplyRepository supplyRepository;
    private final SupplyOperationRepository supplyOperationRepository;
    private final CalculationService calculationService;


    public DatabaseServiceImpl(CategoryRepository categoryRepository, SupplyRepository supplyRepository,
                               SupplyOperationRepository supplyOperationRepository, CalculationService calculationService) {
        this.categoryRepository = categoryRepository;
        this.supplyRepository = supplyRepository;
        this.supplyOperationRepository = supplyOperationRepository;
        this.calculationService = calculationService;
    }

    @Override
    public int getAvailableSupplyByName(String name, String cuig){
        List<CemaSupplyOperation> supplyOperations = supplyOperationRepository.findAllByEstablishmentCuigAndCemaSupplyName(cuig, name);
        int available = 0;
        for (CemaSupplyOperation cemaSupplyOperation: supplyOperations) {
            available += calculationService.getSignedAmount(cemaSupplyOperation);
        }
        return available;
    }

    @Override
    public CemaSupplyOperation saveSupplyOperation(CemaSupplyOperation cemaSupplyOperation, String supplyName) {
        CemaSupply cemaSupply = supplyRepository.findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(supplyName, cemaSupplyOperation.getEstablishmentCuig());
        if (cemaSupply == null) {
            throw new NotFoundException(String.format("The supply %s does not exists", supplyName));
        }
        cemaSupplyOperation.setCemaSupply(cemaSupply);

        return supplyOperationRepository.save(cemaSupplyOperation);
    }

    @Override
    public CemaSupply saveCemaSupply(CemaSupply cemaSupply, String categoryName) {
        Optional<CemaCategory> cemaCategoryOptional = categoryRepository.findById(categoryName);
        if (!cemaCategoryOptional.isPresent()) {
            throw new NotFoundException(String.format("The category %s does not exists", categoryName));
        }
        cemaSupply.setCategory(cemaCategoryOptional.get());

        return supplyRepository.save(cemaSupply);
    }

    @Override
    public Page<CemaSupply> searchSupplies(CemaSupply cemaSupply, String category, int page, int size) {
        if (StringUtils.hasText(category)) {
            CemaCategory cemaCategory = categoryRepository.findCemaCategoryByNameIgnoreCase(category);
            if (cemaCategory == null) {
                throw new NotFoundException(String.format("Category %s not found", category));
            }
            cemaSupply.setCategory(cemaCategory);
        }
        ExampleMatcher caseInsensitiveExampleMatcher = ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Pageable paging = PageRequest.of(page, size, Sort.by("name"));
        return supplyRepository.findAll(Example.of(cemaSupply, caseInsensitiveExampleMatcher), paging);
    }

    @Override
    public boolean canBeDeleted(String supplyName, String cuig) {
        List<CemaSupplyOperation> cemaSupplyOperationList = supplyOperationRepository.findAllByEstablishmentCuigAndCemaSupplyName(cuig, supplyName);
        return CollectionUtils.isEmpty(cemaSupplyOperationList);
    }
}
