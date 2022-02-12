package com.cema.economic.controllers;


import com.cema.economic.domain.BovineOperation;
import com.cema.economic.entities.CemaBovineOperation;
import com.cema.economic.exceptions.NotFoundException;
import com.cema.economic.mapping.Mapping;
import com.cema.economic.repositories.BovineOperationRepository;
import com.cema.economic.services.authorization.AuthorizationService;
import com.cema.economic.services.client.administration.AdministrationClientService;
import com.cema.economic.services.client.bovine.BovineClientService;
import com.cema.economic.services.client.users.UsersClientService;
import com.cema.economic.services.validation.OperationValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class BovineBovineOperationControllerTest {

    private final String id = "b000bba4-229e-4b59-8548-1c26508e459c";
    private final String cuig = "321";
    @Mock
    private BovineOperationRepository bovineOperationRepository;
    @Mock
    private Mapping operationMapping;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private BovineClientService bovineClientService;
    @Mock
    private OperationValidationService bovineOperationValidationService;
    @Mock
    private AdministrationClientService administrationClientService;
    @Mock
    private UsersClientService usersClientService;

    private BovineOperationController bovineOperationController;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        when(authorizationService.isOnTheSameEstablishment(cuig)).thenReturn(true);
        when(authorizationService.getCurrentUserCuig()).thenReturn(cuig);
        bovineOperationController = new BovineOperationController(bovineOperationRepository, operationMapping,
                authorizationService, bovineClientService, bovineOperationValidationService, administrationClientService,
                usersClientService);
    }

    @Test
    public void lookUpOperationByCuigShouldAlwaysReturnOperationWhenExists() {
        CemaBovineOperation cemaBovineOperation = new CemaBovineOperation();
        cemaBovineOperation.setEstablishmentCuig(cuig);
        BovineOperation bovineOperation = BovineOperation.builder().build();


        when(bovineOperationRepository.findCemaBovineOperationById(UUID.fromString(id))).thenReturn(cemaBovineOperation);
        when(operationMapping.mapEntityToDomain(cemaBovineOperation)).thenReturn(bovineOperation);

        ResponseEntity<BovineOperation> result = bovineOperationController.lookUpBovineOperationById(id);
        BovineOperation resultingUser = result.getBody();

        assertThat(resultingUser, is(bovineOperation));
        HttpStatus resultingStatus = result.getStatusCode();

        assertThat(resultingStatus, is(HttpStatus.OK));
    }

    @Test
    public void lookUpOperationByCuigShouldAlwaysReturnNotFoundWhenOperationDoesntExists() {
        CemaBovineOperation cemaBovineOperation = new CemaBovineOperation();
        BovineOperation bovineOperation = BovineOperation.builder().build();

        when(bovineOperationRepository.findCemaBovineOperationById(UUID.fromString(id))).thenReturn(null);
        when(operationMapping.mapEntityToDomain(cemaBovineOperation)).thenReturn(bovineOperation);

        Exception exception = assertThrows(NotFoundException.class, () -> {
            bovineOperationController.lookUpBovineOperationById(id);
        });
        String resultingMessage = exception.getMessage();

        assertThat(resultingMessage, is("Operation with id b000bba4-229e-4b59-8548-1c26508e459c doesn't exits"));
    }

    @Test
    public void registerOperationShouldAlwaysReturnCreatedWhenOperationAddedCorrectly() {
        CemaBovineOperation cemaBovineOperation = new CemaBovineOperation();
        BovineOperation bovineOperation = BovineOperation.builder().build();
        bovineOperation.setEstablishmentCuig(cuig);

        when(operationMapping.mapDomainToEntity(bovineOperation)).thenReturn(cemaBovineOperation);

        ResponseEntity<BovineOperation> result = bovineOperationController.registerBovineOperation(bovineOperation);

        HttpStatus resultingStatus = result.getStatusCode();

        assertThat(resultingStatus, is(HttpStatus.CREATED));
    }

    @Test
    public void updateOperationShouldAlwaysReturnOKWhenOperationUpdatedCorrectly() {
        CemaBovineOperation cemaBovineOperation = new CemaBovineOperation();
        BovineOperation bovineOperation = BovineOperation.builder()
                .establishmentCuig(cuig)
                .build();

        when(bovineOperationRepository.findCemaBovineOperationByIdAndEstablishmentCuigIgnoreCase(any(UUID.class), eq(cuig))).thenReturn(cemaBovineOperation);
        when(operationMapping.mapDomainToEntity(bovineOperation)).thenReturn(cemaBovineOperation);

        ResponseEntity<BovineOperation> result = bovineOperationController.updateBovineOperation(id, bovineOperation, cuig);

        HttpStatus resultingStatus = result.getStatusCode();

        assertThat(resultingStatus, is(HttpStatus.OK));
    }

    @Test
    public void updateOperationShouldAlwaysReturnNotFoundWhenOperationDoesntExists() {
        BovineOperation bovineOperation = BovineOperation.builder().build();
        String cuig = "1233";

        when(bovineOperationRepository.findCemaBovineOperationByIdAndEstablishmentCuigIgnoreCase(any(UUID.class), eq(cuig))).thenReturn(null);


        Exception exception = assertThrows(NotFoundException.class, () ->
                bovineOperationController.updateBovineOperation(id, bovineOperation, cuig));
        String resultingMessage = exception.getMessage();

        assertThat(resultingMessage, is("Operation with id b000bba4-229e-4b59-8548-1c26508e459c doesn't exits"));
    }


}