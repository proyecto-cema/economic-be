package com.cema.economic.controllers;

import com.cema.economic.constants.Messages;
import com.cema.economic.domain.BovineOperation;
import com.cema.economic.entities.CemaBovineOperation;
import com.cema.economic.exceptions.NotFoundException;
import com.cema.economic.exceptions.UnauthorizedException;
import com.cema.economic.mapping.Mapping;
import com.cema.economic.repositories.BovineOperationRepository;
import com.cema.economic.services.authorization.AuthorizationService;
import com.cema.economic.services.client.administration.AdministrationClientService;
import com.cema.economic.services.client.bovine.BovineClientService;
import com.cema.economic.services.client.users.UsersClientService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@Api(produces = "application/json", value = "Allows interaction with the bovine operation database. V1")
@Validated
@Slf4j
public class BovineOperationController {

    private static final String BASE_URL = "/bovine-operations/";

    private final BovineOperationRepository bovineOperationRepository;
    private final Mapping<CemaBovineOperation, BovineOperation> bovineOperationMapping;
    private final AuthorizationService authorizationService;
    private final BovineClientService bovineClientService;
    private final OperationValidationService<BovineOperation> bovineOperationValidationService;
    private final AdministrationClientService administrationClientService;
    private final UsersClientService usersClientService;

    public BovineOperationController(BovineOperationRepository bovineOperationRepository, Mapping<CemaBovineOperation, BovineOperation> bovineOperationMapping,
                                     AuthorizationService authorizationService, BovineClientService bovineClientService,
                                     OperationValidationService<BovineOperation> bovineOperationValidationService,
                                     AdministrationClientService administrationClientService,
                                     UsersClientService usersClientService) {
        this.bovineOperationRepository = bovineOperationRepository;
        this.bovineOperationMapping = bovineOperationMapping;
        this.authorizationService = authorizationService;
        this.bovineClientService = bovineClientService;
        this.bovineOperationValidationService = bovineOperationValidationService;
        this.administrationClientService = administrationClientService;
        this.usersClientService = usersClientService;
    }

    @ApiOperation(value = "Register a new operation to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Operation created successfully"),
            @ApiResponse(code = 409, message = "The operation you were trying to create already exists"),
            @ApiResponse(code = 401, message = "You are not allowed to register this operation")
    })
    @PostMapping(value = BASE_URL, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BovineOperation> registerBovineOperation(
            @ApiParam(
                    value = "Operation data to be inserted.")
            @RequestBody @Valid BovineOperation bovineOperation) {

        log.info("Request to register new operation");

        String cuig = bovineOperation.getEstablishmentCuig();
        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }
        bovineOperationValidationService.validateOperation(bovineOperation);
        administrationClientService.validateEstablishment(cuig);
        usersClientService.validateUser(bovineOperation.getOperatorUserName());

        String tag = bovineOperation.getBovineTag();

        bovineClientService.validateBovine(tag, cuig);

        CemaBovineOperation newOperation = bovineOperationMapping.mapDomainToEntity(bovineOperation);

        newOperation = bovineOperationRepository.save(newOperation);

        BovineOperation updatedBovineOperation = bovineOperationMapping.mapEntityToDomain(newOperation);

        return new ResponseEntity<>(updatedBovineOperation, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Retrieve operation from cuig sent data", response = BovineOperation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found operation"),
            @ApiResponse(code = 404, message = "Operation not found"),
            @ApiResponse(code = 401, message = "You are not allowed to view this operation")
    })
    @GetMapping(value = BASE_URL + "{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BovineOperation> lookUpBovineOperationById(
            @ApiParam(
                    value = "The cuig of the operation you are looking for.",
                    example = "123")
            @PathVariable("id") String id) {

        log.info("Request for operation with id {}", id);

        CemaBovineOperation cemaBovineOperation = bovineOperationRepository.findCemaBovineOperationById(UUID.fromString(id));
        if (cemaBovineOperation == null) {
            throw new NotFoundException(String.format("Operation with id %s doesn't exits", id));
        }
        String cuig = cemaBovineOperation.getEstablishmentCuig();

        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }
        BovineOperation bovineOperation = bovineOperationMapping.mapEntityToDomain(cemaBovineOperation);

        return new ResponseEntity<>(bovineOperation, HttpStatus.OK);
    }

    @ApiOperation(value = "Modifies an existent Operation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation modified successfully"),
            @ApiResponse(code = 404, message = "The operation you were trying to modify doesn't exists"),
            @ApiResponse(code = 401, message = "You are not allowed to update this operation")
    })
    @PutMapping(value = BASE_URL + "{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BovineOperation> updateBovineOperation(
            @ApiParam(
                    value = "The cuig of the operation we are looking for.",
                    example = "123")
            @PathVariable("id") String id,
            @ApiParam(
                    value = "The operation data we are modifying. Cuig cannot be modified and will be ignored.")
            @RequestBody BovineOperation bovineOperation,
            @ApiParam(
                    value = "The cuig of the establishment of the operation. If the user is not admin will be ignored.",
                    example = "321")
            @RequestParam(value = "cuig") String cuig) {

        log.info("Request to modify operation with id: {}", id);

        if (!authorizationService.isAdmin()) {
            cuig = authorizationService.getCurrentUserCuig();
        }
        bovineOperationValidationService.validateOperation(bovineOperation);
        administrationClientService.validateEstablishment(cuig);
        CemaBovineOperation cemaBovineOperation = bovineOperationRepository.findCemaBovineOperationByIdAndEstablishmentCuigIgnoreCase(UUID.fromString(id), cuig);
        if (cemaBovineOperation == null) {
            log.info("Operation doesn't exists");
            throw new NotFoundException(String.format("Operation with id %s doesn't exits", id));
        }

        bovineOperation.setEstablishmentCuig(cuig);

        cemaBovineOperation = bovineOperationMapping.updateDomainWithEntity(bovineOperation, cemaBovineOperation);

        cemaBovineOperation = bovineOperationRepository.save(cemaBovineOperation);

        BovineOperation updatedBovineOperation = bovineOperationMapping.mapEntityToDomain(cemaBovineOperation);

        return new ResponseEntity<>(updatedBovineOperation, HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieve operations for your cuig", response = BovineOperation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Listed all operations", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @GetMapping(value = BASE_URL + "list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<BovineOperation>> listBovineOperations(
            @ApiParam(
                    value = "The page you want to retrieve.",
                    example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(
                    value = "The maximum number of operation entries to return per page.",
                    example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        String cuig = authorizationService.getCurrentUserCuig();
        Pageable paging = PageRequest.of(page, size);

        Page<CemaBovineOperation> cemaOperationPage;
        if (authorizationService.isAdmin()) {
            cemaOperationPage = bovineOperationRepository.findAll(paging);
        } else {
            cemaOperationPage = bovineOperationRepository.findAllByEstablishmentCuig(cuig, paging);
        }

        List<CemaBovineOperation> cemaBovineOperations = cemaOperationPage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaOperationPage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaOperationPage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaOperationPage.getNumber()));

        List<BovineOperation> bovineOperations = cemaBovineOperations.stream().map(bovineOperationMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(bovineOperations);
    }

}
