package com.campusenroll.enrollment_service.integration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StudentServiceClient {

    private final RestClient studentRestClient;

    public StudentServiceClient(
            @Qualifier("studentRestClient") RestClient studentRestClient
    ) {

        this.studentRestClient = studentRestClient;
    }

    public void validateStudentExists(Long studentId) {

        studentRestClient.get()
                .uri("/students/{id}", studentId)
                .retrieve()
                .toBodilessEntity();
    }
}
