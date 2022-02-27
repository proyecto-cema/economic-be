package com.cema.economic.controllers;

import com.cema.economic.constants.Messages;
import com.cema.economic.domain.Supply;
import com.cema.economic.entities.CemaSupply;
import com.cema.economic.entities.CemaSupplyOperation;
import com.cema.economic.exceptions.AlreadyExistsException;
import com.cema.economic.exceptions.NotFoundException;
import com.cema.economic.exceptions.UnauthorizedException;
import com.cema.economic.exceptions.ValidationException;
import com.cema.economic.mapping.Mapping;
import com.cema.economic.repositories.SupplyRepository;
import com.cema.economic.services.authorization.AuthorizationService;
import com.cema.economic.services.client.administration.AdministrationClientService;
import com.cema.economic.services.database.DatabaseService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@Api(produces = "application/json", value = "Allows interaction with the supply database. V1")
@Validated
@Slf4j
public class SupplyController {

    private static final String BASE_URL = "/supply/";

    private final SupplyRepository supplyRepository;
    private final Mapping<CemaSupply, Supply> supplyMapping;
    private final AuthorizationService authorizationService;
    private final AdministrationClientService administrationClientService;
    private final DatabaseService databaseService;

    public SupplyController(SupplyRepository supplyRepository, Mapping<CemaSupply, Supply> supplyMapping,
                            AuthorizationService authorizationService,
                            AdministrationClientService administrationClientService,
                            DatabaseService databaseService) {
        this.supplyRepository = supplyRepository;
        this.supplyMapping = supplyMapping;
        this.authorizationService = authorizationService;
        this.administrationClientService = administrationClientService;
        this.databaseService = databaseService;
    }

    @ApiOperation(value = "Validate supply from name sent data", response = Supply.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Supply is valid"),
            @ApiResponse(code = 404, message = "Supply not found"),
            @ApiResponse(code = 401, message = "You are not allowed to view this supply"),
            @ApiResponse(code = 422, message = "Invalid Supply")
    })
    @GetMapping(value = BASE_URL + "validate/{name}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> validateSupplyByName(
            @ApiParam(
                    value = "The name of the supply you are looking for.",
                    example = "123")
            @PathVariable("name") String name,
            @ApiParam(
                    value = "The cuig of the establishment of the supply. If the user is not admin will be ignored.",
                    example = "312")
            @RequestParam(value = "cuig", required = false) String cuig) {

        log.info("Request to validate supply with {}", name);

        if (!authorizationService.isAdmin() || !StringUtils.hasLength(cuig)) {
            cuig = authorizationService.getCurrentUserCuig();
        }

        CemaSupply cemaSupply = supplyRepository.findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(name, cuig);
        if (cemaSupply == null) {
            throw new NotFoundException(String.format("Supply with name %s doesn't exits", name));
        }

        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Register a new supply to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Supply created successfully"),
            @ApiResponse(code = 409, message = "The supply you were trying to create already exists"),
            @ApiResponse(code = 401, message = "You are not allowed to register this supply")
    })
    @PostMapping(value = BASE_URL, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Supply> registerSupply(
            @ApiParam(
                    value = "Supply data to be inserted.")
            @RequestBody @Valid Supply supply) {

        log.info("Request to register new supply");

        String cuig = supply.getEstablishmentCuig();
        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }

        CemaSupply cemaSupply = supplyRepository.findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(supply.getName(), cuig);
        if (cemaSupply != null) {
            throw new AlreadyExistsException(String.format("Supply with name %s already exits", supply.getName()));
        }
        administrationClientService.validateEstablishment(cuig);

        CemaSupply newSupply = supplyMapping.mapDomainToEntity(supply);

        newSupply = databaseService.saveCemaSupply(newSupply, supply.getCategoryName());

        Supply updatedSupply = supplyMapping.mapEntityToDomain(newSupply);

        return new ResponseEntity<>(updatedSupply, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Retrieve supply from cuig sent data", response = Supply.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found supply"),
            @ApiResponse(code = 404, message = "Supply not found")
    })
    @GetMapping(value = BASE_URL + "{name}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Supply> lookUpSupplyByName(
            @ApiParam(
                    value = "The cuig of the supply you are looking for.",
                    example = "123")
            @PathVariable("name") String name,
            @ApiParam(
                    value = "The cuig of the establishment of the supply. If the user is not admin will be ignored.",
                    example = "312")
            @RequestParam(value = "cuig", required = false) String cuig) {

        log.info("Request for supply with name {}", name);

        if (!authorizationService.isAdmin() || !StringUtils.hasLength(cuig)) {
            cuig = authorizationService.getCurrentUserCuig();
        }

        CemaSupply cemaSupply = supplyRepository.findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(name, cuig);
        if (cemaSupply == null) {
            throw new NotFoundException(String.format("Supply with name %s doesn't exits", name));
        }

        Supply supply = supplyMapping.mapEntityToDomain(cemaSupply);

        return new ResponseEntity<>(supply, HttpStatus.OK);
    }

    @ApiOperation(value = "Modifies an existent Supply")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Supply modified successfully"),
            @ApiResponse(code = 404, message = "The supply you were trying to modify doesn't exists"),
            @ApiResponse(code = 401, message = "You are not allowed to update this supply")
    })
    @PutMapping(value = BASE_URL + "{name}", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Supply> updateSupply(
            @ApiParam(
                    value = "The name of the supply we are looking for.",
                    example = "123")
            @PathVariable("name") String name,
            @ApiParam(
                    value = "The supply data we are modifying. Cuig cannot be modified and will be ignored.")
            @RequestBody Supply supply,
            @ApiParam(
                    value = "The cuig of the establishment of the supply. If the user is not admin will be ignored.",
                    example = "321")
            @RequestParam(value = "cuig") String cuig) {

        log.info("Request to modify supply with name: {}", name);

        if (!authorizationService.isAdmin()) {
            cuig = authorizationService.getCurrentUserCuig();
        }
        administrationClientService.validateEstablishment(cuig);
        CemaSupply cemaSupply = supplyRepository.findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(name, cuig);
        if (cemaSupply == null) {
            log.info("Supply doesn't exists");
            throw new NotFoundException(String.format("Supply with name %s doesn't exits", name));
        }

        supply.setEstablishmentCuig(cuig);

        cemaSupply = supplyMapping.updateDomainWithEntity(supply, cemaSupply);

        cemaSupply = supplyRepository.save(cemaSupply);

        Supply updatedSupply = supplyMapping.mapEntityToDomain(cemaSupply);

        return new ResponseEntity<>(updatedSupply, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete an existing supply by name")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Supply deleted successfully"),
            @ApiResponse(code = 404, message = "The supply you were trying to reach is not found")
    })
    @DeleteMapping(value = BASE_URL + "{name}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Supply> deleteSupply(
            @ApiParam(
                    value = "The name for the supply we are looking for.",
                    example = "123")
            @PathVariable("name") String name,
            @ApiParam(
                    value = "The cuig of the establishment of the supply. If the user is not admin will be ignored.",
                    example = "312")
            @RequestParam(value = "cuig", required = false) String cuig) {

        if (!authorizationService.isAdmin() || !StringUtils.hasLength(cuig)) {
            cuig = authorizationService.getCurrentUserCuig();
        }

        log.info("Request to delete supply with name {} and cuig {}", name, cuig);
        CemaSupply cemaSupply = supplyRepository.findCemaSupplyByNameAndEstablishmentCuigIgnoreCase(name, cuig);
        if (cemaSupply != null) {
            if(!authorizationService.isAdmin() && !databaseService.canBeDeleted(name, cuig)){
                throw new ValidationException(String.format("Supply %s cannot be deleted because it has operations associated", name));
            }
            log.info("Supply exists, deleting");
            supplyRepository.delete(cemaSupply);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        log.info("Not found");
        throw new NotFoundException(String.format("Supply %s doesn't exits", name));
    }

    @ApiOperation(value = "Retrieve supplies for your cuig", response = Supply.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Listed all supplys", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @GetMapping(value = BASE_URL + "list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Supply>> listSupplies(
            @ApiParam(
                    value = "The page you want to retrieve.",
                    example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(
                    value = "The maximum number of supply entries to return per page.",
                    example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        String cuig = authorizationService.getCurrentUserCuig();
        Pageable paging = PageRequest.of(page, size);

        Page<CemaSupply> cemaSupplyPage;
        if (authorizationService.isAdmin()) {
            cemaSupplyPage = supplyRepository.findAll(paging);
        } else {
            cemaSupplyPage = supplyRepository.findAllByEstablishmentCuig(cuig, paging);
        }

        List<CemaSupply> cemaSupplies = cemaSupplyPage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaSupplyPage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaSupplyPage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaSupplyPage.getNumber()));

        List<Supply> supplies = cemaSupplies.stream().map(supplyMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(supplies);
    }

    @ApiOperation(value = "Retrieve a list of supplies matching the sent data", response = Supply.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found supplies", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @PostMapping(value = BASE_URL + "search", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Supply>> searchSupplies(
            @ApiParam(
                    value = "The page you want to retrieve.",
                    example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(
                    value = "The maximum number of supplies to return per page.",
                    example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "3") int size,
            @ApiParam(
                    value = "The supply data we are searching")
            @RequestBody Supply supply) {

        if (!authorizationService.isAdmin()) {
            supply.setEstablishmentCuig(authorizationService.getCurrentUserCuig());
        }

        CemaSupply cemaSupply = supplyMapping.mapDomainToEntity(supply);

        Page<CemaSupply> cemaSupplyPage = databaseService.searchSupplies(cemaSupply, supply.getCategoryName(), page, size);

        List<CemaSupply> cemaSupplies = cemaSupplyPage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaSupplyPage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaSupplyPage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaSupplyPage.getNumber()));

        List<Supply> supplies = cemaSupplies.stream().map(supplyMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(supplies);
    }
}
