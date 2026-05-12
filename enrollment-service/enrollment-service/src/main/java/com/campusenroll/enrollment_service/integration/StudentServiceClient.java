package com.campusenroll.enrollment_service.integration;

import com.campusenroll.enrollment_service.exception.BusinessException;
import com.campusenroll.enrollment_service.exception.ResourceNotFoundException;
import com.campusenroll.enrollment_service.integration.dto.StudentStatusResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class StudentServiceClient {

    private final RestClient studentRestClient;

    public StudentServiceClient(
            @Qualifier("studentRestClient") RestClient studentRestClient
    ) {

        this.studentRestClient = studentRestClient;
    }

    public StudentStatusResponse getStudentStatus(Long studentId) {
        try {
            StudentStatusResponse response = studentRestClient.get()
                    .uri("/students/{id}/status", studentId)
                    .retrieve()
                    .body(StudentStatusResponse.class);
            if (response == null) {
                throw new ResourceNotFoundException("No se obtuvo estado del estudiante " + studentId);
            }
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Estudiante no encontrado: " + studentId);
            }
            throw new BusinessException("Error consultando student-service");
        }
    }
}
