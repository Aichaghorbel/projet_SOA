package com.educampus.academic.dto;

import com.educampus.academic.entity.Result;
import com.educampus.academic.entity.ResultSource;
import com.educampus.academic.entity.ResultStatus;
import com.educampus.academic.entity.SessionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResultResponse(
        Long id,
        Long enrollmentId,
        Long examId,
        BigDecimal grade,
        SessionType sessionType,
        ResultStatus status,
        ResultSource source,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime publishedAt) {

    public static ResultResponse from(Result r) {
        return new ResultResponse(r.getId(), r.getEnrollmentId(), r.getExamId(), r.getGrade(),
                r.getSessionType(), r.getStatus(), r.getSource(), r.getComment(),
                r.getCreatedAt(), r.getPublishedAt());
    }
}