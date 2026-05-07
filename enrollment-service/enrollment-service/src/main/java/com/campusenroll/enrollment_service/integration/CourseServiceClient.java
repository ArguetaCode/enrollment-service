package com.campusenroll.enrollment_service.integration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CourseServiceClient {

    private final RestClient courseRestClient;

    public CourseServiceClient(
            @Qualifier("courseRestClient") RestClient courseRestClient
    ) {

        this.courseRestClient = courseRestClient;
    }

    public void validateSectionExists(Long sectionId) {

        courseRestClient.get()
                .uri("/sections/{id}", sectionId)
                .retrieve()
                .toBodilessEntity();
    }

    public void reserveSeat(Long sectionId) {

        courseRestClient.post()
                .uri("/sections/{id}/reserve-seat", sectionId)
                .retrieve()
                .toBodilessEntity();
    }

    public void confirmSeat(Long sectionId) {

        courseRestClient.post()
                .uri("/sections/{id}/confirm-seat", sectionId)
                .retrieve()
                .toBodilessEntity();
    }

    public void releaseSeat(Long sectionId) {

        courseRestClient.post()
                .uri("/sections/{id}/release-seat", sectionId)
                .retrieve()
                .toBodilessEntity();
    }
}
