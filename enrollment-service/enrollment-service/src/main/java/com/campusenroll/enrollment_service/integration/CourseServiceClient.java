package com.campusenroll.enrollment_service.integration;

import com.campusenroll.enrollment_service.exception.BusinessException;
import com.campusenroll.enrollment_service.exception.ResourceNotFoundException;
import com.campusenroll.enrollment_service.integration.dto.CourseSectionResponse;
import com.campusenroll.enrollment_service.integration.dto.SectionScheduleViewResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CourseServiceClient {

    private final RestClient courseRestClient;

    public CourseServiceClient(
            @Qualifier("courseRestClient") RestClient courseRestClient
    ) {

        this.courseRestClient = courseRestClient;
    }

    public CourseSectionResponse getSection(Long sectionId) {
        try {
            CourseSectionResponse response = courseRestClient.get()
                    .uri("/sections/{id}", sectionId)
                    .retrieve()
                    .body(CourseSectionResponse.class);
            if (response == null) {
                throw new ResourceNotFoundException("Seccion no encontrada: " + sectionId);
            }
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Seccion no encontrada: " + sectionId);
            }
            throw new BusinessException("Error consultando course-service");
        }
    }

    public List<SectionScheduleViewResponse> getSectionSchedule(Long sectionId) {
        try {
            SectionScheduleViewResponse[] schedules = courseRestClient.get()
                    .uri("/sections/{id}/schedule", sectionId)
                    .retrieve()
                    .body(SectionScheduleViewResponse[].class);
            if (schedules == null) {
                return List.of();
            }
            return Arrays.asList(schedules);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Horario de seccion no encontrado: " + sectionId);
            }
            throw new BusinessException("Error consultando horarios en course-service");
        }
    }

    public void reserveSeat(Long sectionId) {
        try {
            courseRestClient.post()
                    .uri("/sections/{id}/reserve-seat", sectionId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new BusinessException("No se pudo reservar cupo en sectionId " + sectionId);
        }
    }

    public void confirmSeat(Long sectionId) {
        try {
            courseRestClient.post()
                    .uri("/sections/{id}/confirm-seat", sectionId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new BusinessException("No se pudo confirmar cupo en sectionId " + sectionId);
        }
    }

    public void releaseSeat(Long sectionId) {
        try {
            courseRestClient.post()
                    .uri("/sections/{id}/release-seat", sectionId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new BusinessException("No se pudo liberar cupo en sectionId " + sectionId);
        }
    }
}
