package com.cema.economic.controllers;


import com.cema.economic.domain.Operation;
import com.cema.economic.entities.CemaOperation;
import com.cema.economic.exceptions.NotFoundException;
import com.cema.economic.mapping.Mapping;
import com.cema.economic.repositories.OperationRepository;
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

public class OperationControllerTest {

    private final String id = "b000bba4-229e-4b59-8548-1c26508e459c";
    private final String cuig = "321";
    @Mock
    private OperationRepository operationRepository;
    @Mock
    private Mapping operationMapping;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private BovineClientService bovineClientService;
    @Mock
    private OperationValidationService operationValidationService;
    @Mock
    private AdministrationClientService administrationClientService;
    @Mock
    private UsersClientService usersClientService;

    private OperationController operationController;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        when(authorizationService.isOnTheSameEstablishment(cuig)).thenReturn(true);
        when(authorizationService.getCurrentUserCuig()).thenReturn(cuig);
        operationController = new OperationController(operationRepository, operationMapping,
                authorizationService, bovineClientService, operationValidationService, administrationClientService,
                usersClientService);
    }

    @Test
    public void lookUpOperationByCuigShouldAlwaysReturnOperationWhenExists() {
        CemaOperation cemaOperation = new CemaOperation();
        cemaOperation.setEstablishmentCuig(cuig);
        Operation operation = Operation.builder().build();


        when(operationRepository.findCemaOperationById(UUID.fromString(id))).thenReturn(cemaOperation);
        when(operationMapping.mapEntityToDomain(cemaOperation)).thenReturn(operation);

        ResponseEntity<Operation> result = operationController.lookUpOperationById(id);
        Operation resultingUser = result.getBody();

        assertThat(resultingUser, is(operation));
        HttpStatus resultingStatus = result.getStatusCode();

        assertThat(resultingStatus, is(HttpStatus.OK));
    }

    @Test
    public void lookUpOperationByCuigShouldAlwaysReturnNotFoundWhenOperationDoesntExists() {
        CemaOperation cemaOperation = new CemaOperation();
        Operation operation = Operation.builder().build();

        when(operationRepository.findCemaOperationById(UUID.fromString(id))).thenReturn(null);
        when(operationMapping.mapEntityToDomain(cemaOperation)).thenReturn(operation);

        Exception exception = assertThrows(NotFoundException.class, () -> {
            operationController.lookUpOperationById(id);
        });
        String resultingMessage = exception.getMessage();

        assertThat(resultingMessage, is("Operation with id b000bba4-229e-4b59-8548-1c26508e459c doesn't exits"));
    }

    @Test
    public void registerOperationShouldAlwaysReturnCreatedWhenOperationAddedCorrectly() {
        CemaOperation cemaOperation = new CemaOperation();
        Operation operation = Operation.builder().build();
        operation.setEstablishmentCuig(cuig);

        when(operationMapping.mapDomainToEntity(operation)).thenReturn(cemaOperation);

        ResponseEntity<Operation> result = operationController.registerOperation(operation);

        HttpStatus resultingStatus = result.getStatusCode();

        assertThat(resultingStatus, is(HttpStatus.CREATED));
    }

    @Test
    public void updateOperationShouldAlwaysReturnOKWhenOperationUpdatedCorrectly() {
        CemaOperation cemaOperation = new CemaOperation();
        Operation operation = Operation.builder()
                .establishmentCuig(cuig)
                .build();

        when(operationRepository.findCemaOperationByIdAndEstablishmentCuigIgnoreCase(any(UUID.class), eq(cuig))).thenReturn(cemaOperation);
        when(operationMapping.mapDomainToEntity(operation)).thenReturn(cemaOperation);

        ResponseEntity<Operation> result = operationController.updateOperation(id, operation, cuig);

        HttpStatus resultingStatus = result.getStatusCode();

        assertThat(resultingStatus, is(HttpStatus.OK));
    }

    @Test
    public void updateOperationShouldAlwaysReturnNotFoundWhenOperationDoesntExists() {
        Operation operation = Operation.builder().build();
        String cuig = "1233";

        when(operationRepository.findCemaOperationByIdAndEstablishmentCuigIgnoreCase(any(UUID.class), eq(cuig))).thenReturn(null);


        Exception exception = assertThrows(NotFoundException.class, () ->
                operationController.updateOperation(id, operation, cuig));
        String resultingMessage = exception.getMessage();

        assertThat(resultingMessage, is("Operation with id b000bba4-229e-4b59-8548-1c26508e459c doesn't exits"));
    }


}