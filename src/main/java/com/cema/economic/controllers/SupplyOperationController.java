package com.cema.economic.controllers;

import com.cema.economic.constants.Messages;
import com.cema.economic.constants.OperationType;
import com.cema.economic.domain.AvailableSupply;
import com.cema.economic.domain.BovineOperation;
import com.cema.economic.domain.SupplyOperation;
import com.cema.economic.entities.CemaSupply;
import com.cema.economic.entities.CemaSupplyOperation;
import com.cema.economic.exceptions.NotFoundException;
import com.cema.economic.exceptions.UnauthorizedException;
import com.cema.economic.mapping.Mapping;
import com.cema.economic.repositories.SupplyOperationRepository;
import com.cema.economic.repositories.SupplyRepository;
import com.cema.economic.services.authorization.AuthorizationService;
import com.cema.economic.services.client.administration.AdministrationClientService;
import com.cema.economic.services.client.users.UsersClientService;
import com.cema.economic.services.database.DatabaseService;
import com.cema.economic.services.validation.OperationValidationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@Api(produces = "application/json", value = "Allows interaction with the supply operation database. V1")
@Validated
@Slf4j
public class SupplyOperationController {

    private static final String BASE_URL = "/supply-operations/";

    private final SupplyOperationRepository supplyOperationRepository;
    private final Mapping<CemaSupplyOperation, SupplyOperation> supplyOperationMapping;
    private final AuthorizationService authorizationService;
    private final OperationValidationService<SupplyOperation> supplyOperationValidationService;
    private final AdministrationClientService administrationClientService;
    private final UsersClientService usersClientService;
    private final DatabaseService databaseService;
    private final SupplyRepository supplyRepository;

    public SupplyOperationController(SupplyOperationRepository supplyOperationRepository,
                                     Mapping<CemaSupplyOperation, SupplyOperation> supplyOperationMapping,
                                     AuthorizationService authorizationService,
                                     OperationValidationService<SupplyOperation> supplyOperationValidationService,
                                     AdministrationClientService administrationClientService,
                                     UsersClientService usersClientService,
                                     DatabaseService databaseService,
                                     SupplyRepository supplyRepository) {
        this.supplyOperationRepository = supplyOperationRepository;
        this.supplyOperationMapping = supplyOperationMapping;
        this.authorizationService = authorizationService;
        this.supplyOperationValidationService = supplyOperationValidationService;
        this.administrationClientService = administrationClientService;
        this.usersClientService = usersClientService;
        this.databaseService = databaseService;
        this.supplyRepository = supplyRepository;
    }

    @ApiOperation(value = "Retrieve availability for a supply")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found availability for supply"),
            @ApiResponse(code = 404, message = "Supply not found")
    })
    @GetMapping(value = BASE_URL + "available/{name}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AvailableSupply> getAvailableSupply(
            @ApiParam(
                    value = "The name of the supply.",
                    example = "Maiz")
            @PathVariable("name") String name,
            @ApiParam(
                    value = "The cuig of the establishment of the operation. If the user is not admin will be ignored.",
                    example = "321")
            @RequestParam(value = "cuig") String cuig) {

        log.info("Request for availability of supply  {}", name);

        if (!authorizationService.isAdmin()) {
            cuig = authorizationService.getCurrentUserCuig();
        }

        CemaSupply cemaSupply = supplyRepository.findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(name, cuig);
        if (cemaSupply == null) {
            throw new NotFoundException(String.format("Supply %s not found", name));
        }

        long available = databaseService.getAvailableSupplyByName(name, cuig);

        AvailableSupply availableSupply = AvailableSupply.builder()
                .available(available)
                .supplyName(name)
                .units(cemaSupply.getUnits())
                .build();

        return ResponseEntity.ok(availableSupply);
    }

    @ApiOperation(value = "Retrieve availability for all supplies")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Found supplies to report"),
            @ApiResponse(code = 404, message = "Supplies not found for this establishment")
    })
    @GetMapping(value = BASE_URL + "available", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<AvailableSupply>> getAllAvailableSupplies(
            @ApiParam(
                    value = "The cuig of the establishment of the operation. If the user is not admin will be ignored.",
                    example = "321")
            @RequestParam(value = "cuig") String cuig) {

        log.info("Request for availability of supplies");

        if (!authorizationService.isAdmin()) {
            cuig = authorizationService.getCurrentUserCuig();
        }

        List<String> allSupplies = supplyRepository.findAllAvailableSupplies(cuig);

        if (CollectionUtils.isEmpty(allSupplies)) {
            throw new NotFoundException(String.format("This establishment %s has no supplies loaded", cuig));
        }

        List<AvailableSupply> availableSupplies = new ArrayList<>();

        for (String supplyName : allSupplies) {
            CemaSupply cemaSupply = supplyRepository.findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(supplyName, cuig);
            long available = databaseService.getAvailableSupplyByName(supplyName, cuig);
            if (cemaSupply != null) {
                AvailableSupply availableSupply = AvailableSupply.builder()
                        .units(cemaSupply.getUnits())
                        .available(available)
                        .supplyName(supplyName)
                        .build();
                availableSupplies.add(availableSupply);
            }
        }
        return ResponseEntity.ok(availableSupplies);
    }

    @ApiOperation(value = "Register a new supply operation to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Operation created successfully"),
            @ApiResponse(code = 409, message = "The supply operation you were trying to create already exists"),
            @ApiResponse(code = 401, message = "You are not allowed to register this operation")
    })
    @PostMapping(value = BASE_URL, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<SupplyOperation> registerSupplyOperation(
            @ApiParam(
                    value = "Operation data to be inserted.")
            @RequestBody @Valid SupplyOperation supplyOperation) {

        log.info("Request to register new operation");

        String cuig = supplyOperation.getEstablishmentCuig();
        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }
        supplyOperationValidationService.validateOperation(supplyOperation);

        usersClientService.validateUser(supplyOperation.getOperatorUserName());

        CemaSupplyOperation newOperation = supplyOperationMapping.mapDomainToEntity(supplyOperation);

        newOperation = databaseService.saveSupplyOperation(newOperation, supplyOperation.getSupplyName());

        SupplyOperation updatedSupplyOperation = supplyOperationMapping.mapEntityToDomain(newOperation);

        return new ResponseEntity<>(updatedSupplyOperation, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Retrieve supply operation from cuig sent data", response = SupplyOperation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found operation"),
            @ApiResponse(code = 404, message = "Operation not found"),
            @ApiResponse(code = 401, message = "You are not allowed to view this operation")
    })
    @GetMapping(value = BASE_URL + "{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<SupplyOperation> lookUpSupplyOperationById(
            @ApiParam(
                    value = "The cuig of the supply operation you are looking for.",
                    example = "123")
            @PathVariable("id") String id) {

        log.info("Request for supply operation with id {}", id);

        CemaSupplyOperation cemaSupplyOperation = supplyOperationRepository.findCemaSupplyOperationById(UUID.fromString(id));
        if (cemaSupplyOperation == null) {
            throw new NotFoundException(String.format("Operation with id %s doesn't exits", id));
        }
        String cuig = cemaSupplyOperation.getEstablishmentCuig();

        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }
        SupplyOperation supplyOperation = supplyOperationMapping.mapEntityToDomain(cemaSupplyOperation);

        return new ResponseEntity<>(supplyOperation, HttpStatus.OK);
    }


    @ApiOperation(value = "Modifies an existent Operation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation modified successfully"),
            @ApiResponse(code = 404, message = "The supply operation you were trying to modify doesn't exists"),
            @ApiResponse(code = 401, message = "You are not allowed to update this operation")
    })
    @PutMapping(value = BASE_URL + "{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<SupplyOperation> updateSupplyOperation(
            @ApiParam(
                    value = "The cuig of the supply operation we are looking for.",
                    example = "123")
            @PathVariable("id") String id,
            @ApiParam(
                    value = "The supply operation data we are modifying. Cuig cannot be modified and will be ignored.")
            @RequestBody SupplyOperation supplyOperation,
            @ApiParam(
                    value = "The cuig of the establishment of the operation. If the user is not admin will be ignored.",
                    example = "321")
            @RequestParam(value = "cuig") String cuig) {

        log.info("Request to modify supply operation with id: {}", id);

        if (!authorizationService.isAdmin()) {
            cuig = authorizationService.getCurrentUserCuig();
        }
        supplyOperationValidationService.validateOperation(supplyOperation);
        administrationClientService.validateEstablishment(cuig);
        CemaSupplyOperation cemaSupplyOperation = supplyOperationRepository.findCemaSupplyOperationByIdAndEstablishmentCuigIgnoreCase(UUID.fromString(id), cuig);
        if (cemaSupplyOperation == null) {
            log.info("Operation doesn't exists");
            throw new NotFoundException(String.format("Operation with id %s doesn't exits", id));
        }

        supplyOperation.setEstablishmentCuig(cuig);

        cemaSupplyOperation = supplyOperationMapping.updateDomainWithEntity(supplyOperation, cemaSupplyOperation);

        cemaSupplyOperation = supplyOperationRepository.save(cemaSupplyOperation);

        SupplyOperation updatedSupplyOperation = supplyOperationMapping.mapEntityToDomain(cemaSupplyOperation);

        return new ResponseEntity<>(updatedSupplyOperation, HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieve operations for your cuig", response = SupplyOperation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Listed all operations", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @GetMapping(value = BASE_URL + "list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SupplyOperation>> listSupplyOperations(
            @ApiParam(
                    value = "The page you want to retrieve.",
                    example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(
                    value = "The maximum number of supply operation entries to return per page.",
                    example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        String cuig = authorizationService.getCurrentUserCuig();
        Pageable paging = PageRequest.of(page, size, Sort.by("transactionDate").descending());

        Page<CemaSupplyOperation> cemaOperationPage;
        if (authorizationService.isAdmin()) {
            cemaOperationPage = supplyOperationRepository.findAll(paging);
        } else {
            cemaOperationPage = supplyOperationRepository.findAllByEstablishmentCuig(cuig, paging);
        }

        List<CemaSupplyOperation> cemaSupplyOperations = cemaOperationPage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaOperationPage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaOperationPage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaOperationPage.getNumber()));

        List<SupplyOperation> supplyOperations = cemaSupplyOperations.stream().map(supplyOperationMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(supplyOperations);
    }

    @ApiOperation(value = "Retrieve the total for spending in supplies", response = BovineOperation.class)
    @ApiResponse(code = 200, message = "Returned total spending")
    @GetMapping(value = BASE_URL + "total", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Long> getTotal(
            @ApiParam(
                    value = "The cuig of the establishment of the operation. If the user is not admin will be ignored.",
                    example = "321")
            @RequestParam(value = "cuig") String cuig) {

        if (!authorizationService.isAdmin()) {
            cuig = authorizationService.getCurrentUserCuig();
        }

        Long spending = supplyOperationRepository.getSumForOperationType(cuig, OperationType.BUY);

        return ResponseEntity.ok().body(spending);
    }

}
