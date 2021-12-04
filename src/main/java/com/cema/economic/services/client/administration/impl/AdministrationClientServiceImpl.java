package com.cema.economic.services.client.administration.impl;

import com.cema.economic.domain.ErrorResponse;
import com.cema.economic.exceptions.ValidationException;
import com.cema.economic.services.authorization.AuthorizationService;
import com.cema.economic.services.client.administration.AdministrationClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class AdministrationClientServiceImpl implements AdministrationClientService {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String PATH_VALIDATE_ESTABLISHMENT = "establishment/validate/{cuig}";

    private final RestTemplate restTemplate;
    private final String url;
    private final AuthorizationService authorizationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AdministrationClientServiceImpl(RestTemplate restTemplate, @Value("${back-end.administration.url}") String url,
                                           AuthorizationService authorizationService) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.authorizationService = authorizationService;
    }

    @SneakyThrows
    @Override
    public void validateEstablishment(String cuig) {
        String authToken = authorizationService.getUserAuthToken();
        String searchUrl = url + PATH_VALIDATE_ESTABLISHMENT;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, authToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity("{}", httpHeaders);
        try {
            restTemplate.exchange(searchUrl, HttpMethod.GET, entity, Object.class, cuig);
        } catch (RestClientResponseException httpClientErrorException) {
            String response = httpClientErrorException.getResponseBodyAsString();
            ErrorResponse errorResponse = mapper.readValue(response, ErrorResponse.class);
            throw new ValidationException(errorResponse.getMessage(), httpClientErrorException);
        }
    }
}
