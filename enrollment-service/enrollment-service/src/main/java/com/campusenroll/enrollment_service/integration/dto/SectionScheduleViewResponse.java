package com.campusenroll.enrollment_service.integration.dto;

import java.time.LocalTime;

public record SectionScheduleViewResponse(
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
