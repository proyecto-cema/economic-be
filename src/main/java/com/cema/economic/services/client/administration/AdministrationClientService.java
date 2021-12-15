package com.cema.economic.services.client.administration;

import com.cema.economic.domain.audit.Audit;

public interface AdministrationClientService {

    void validateEstablishment(String cuig);

    void sendAuditRequest(Audit audit);
}
