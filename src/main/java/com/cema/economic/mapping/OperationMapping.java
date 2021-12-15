package com.cema.economic.mapping;

import com.cema.economic.domain.Operation;
import com.cema.economic.entities.CemaOperation;

public interface OperationMapping {

    Operation mapEntityToDomain(CemaOperation cemaOperation);

    CemaOperation mapDomainToEntity(Operation operation);

    CemaOperation updateDomainWithEntity(Operation operation, CemaOperation cemaOperation);
}
