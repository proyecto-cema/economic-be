package com.cema.economic.mapping;

import com.cema.economic.domain.Operation;
import com.cema.economic.entities.CemaOperation;

public interface Mapping<ENTITY, DOMAIN> {

    DOMAIN mapEntityToDomain(ENTITY entity);

    ENTITY mapDomainToEntity(DOMAIN domain);

    ENTITY updateDomainWithEntity(DOMAIN domain, ENTITY entity);
}
