package com.cema.economic.controllers;

import com.cema.economic.constants.Messages;
import com.cema.economic.domain.Operation;
import com.cema.economic.entities.CemaOperation;
import com.cema.economic.exceptions.NotFoundException;
import com.cema.economic.exceptions.UnauthorizedException;
import com.cema.economic.mapping.OperationMapping;
import com.cema.economic.repositories.OperationRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Api(produces = "application/json", value = "Allows interaction with the operation database. V1")
@Validated
public class OperationController {

    private static final String BASE_URL = "/operation/";

    private final Logger LOG = LoggerFactory.getLogger(OperationController.class);

    private final OperationRepository operationRepository;
    private final OperationMapping operationMapping;
    private final AuthorizationService authorizationService;
    private final BovineClientService bovineClientService;
    private final OperationValidationService operationValidationService;
    private final AdministrationClientService administrationClientService;
    private final UsersClientService usersClientService;

    public OperationController(OperationRepository operationRepository, OperationMapping operationMapping,
                               AuthorizationService authorizationService, BovineClientService bovineClientService,
                               OperationValidationService operationValidationService,
                               AdministrationClientService administrationClientService,
                               UsersClientService usersClientService) {
        this.operationRepository = operationRepository;
        this.operationMapping = operationMapping;
        this.authorizationService = authorizationService;
        this.bovineClientService = bovineClientService;
        this.operationValidationService = operationValidationService;
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
    public ResponseEntity<Operation> registerOperation(
            @ApiParam(
                    value = "Operation data to be inserted.")
            @RequestBody @Valid Operation operation) {

        LOG.info("Request to register new operation");

        String cuig = operation.getEstablishmentCuig();
        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }
        operationValidationService.validateOperation(operation);
        administrationClientService.validateEstablishment(cuig);
        usersClientService.validateUser(operation.getOperatorUserName());

        String tag = operation.getBovineTag();

        bovineClientService.validateBovine(tag, cuig);

        CemaOperation newOperation = operationMapping.mapDomainToEntity(operation);

        newOperation = operationRepository.save(newOperation);

        Operation updatedOperation = operationMapping.mapEntityToDomain(newOperation);

        return new ResponseEntity<>(updatedOperation, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Retrieve operation from cuig sent data", response = Operation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found operation"),
            @ApiResponse(code = 404, message = "Operation not found"),
            @ApiResponse(code = 401, message = "You are not allowed to view this operation")
    })
    @GetMapping(value = BASE_URL + "{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Operation> lookUpOperationById(
            @ApiParam(
                    value = "The cuig of the operation you are looking for.",
                    example = "123")
            @PathVariable("id") String id) {

        LOG.info("Request for operation with id {}", id);

        CemaOperation cemaOperation = operationRepository.findCemaOperationById(UUID.fromString(id));
        if (cemaOperation == null) {
            throw new NotFoundException(String.format("Operation with id %s doesn't exits", id));
        }
        String cuig = cemaOperation.getEstablishmentCuig();

        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }
        Operation operation = operationMapping.mapEntityToDomain(cemaOperation);

        return new ResponseEntity<>(operation, HttpStatus.OK);
    }

    @ApiOperation(value = "Modifies an existent Operation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation modified successfully"),
            @ApiResponse(code = 404, message = "The operation you were trying to modify doesn't exists"),
            @ApiResponse(code = 401, message = "You are not allowed to update this operation")
    })
    @PutMapping(value = BASE_URL + "{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Operation> updateOperation(
            @ApiParam(
                    value = "The cuig of the operation we are looking for.",
                    example = "123")
            @PathVariable("id") String id,
            @ApiParam(
                    value = "The operation data we are modifying. Cuig cannot be modified and will be ignored.")
            @RequestBody Operation operation,
            @ApiParam(
                    value = "The cuig of the establishment of the operation. If the user is not admin will be ignored.",
                    example = "321")
            @RequestParam(value = "cuig") String cuig) {

        LOG.info("Request to modify operation with id: {}", id);

        if (!authorizationService.isAdmin()) {
            cuig = authorizationService.getCurrentUserCuig();
        }
        operationValidationService.validateOperation(operation);
        administrationClientService.validateEstablishment(cuig);
        CemaOperation cemaOperation = operationRepository.findCemaOperationByIdAndEstablishmentCuigIgnoreCase(UUID.fromString(id), cuig);
        if (cemaOperation == null) {
            LOG.info("Operation doesn't exists");
            throw new NotFoundException(String.format("Operation with id %s doesn't exits", id));
        }

        operation.setEstablishmentCuig(cuig);

        cemaOperation = operationMapping.updateDomainWithEntity(operation, cemaOperation);

        cemaOperation = operationRepository.save(cemaOperation);

        Operation updatedOperation = operationMapping.mapEntityToDomain(cemaOperation);

        return new ResponseEntity<>(updatedOperation, HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieve operations for your cuig", response = Operation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Listed all operations", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @GetMapping(value = BASE_URL + "list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Operation>> listOperations(
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

        Page<CemaOperation> cemaOperationPage;
        if (authorizationService.isAdmin()) {
            cemaOperationPage = operationRepository.findAll(paging);
        } else {
            cemaOperationPage = operationRepository.findAllByEstablishmentCuig(cuig, paging);
        }

        List<CemaOperation> cemaOperations = cemaOperationPage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaOperationPage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaOperationPage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaOperationPage.getNumber()));

        List<Operation> operations = cemaOperations.stream().map(operationMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(operations);
    }

}
