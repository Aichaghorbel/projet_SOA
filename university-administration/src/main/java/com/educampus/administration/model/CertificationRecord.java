package com.educampus.administration.model;

import com.educampus.administration.generated.CertificationStatus;
import com.educampus.administration.generated.CertificationType;
import java.time.OffsetDateTime;

public class CertificationRecord {
    private final String requestId;
    private final String studentId;
    private final CertificationType type;
    private final String comment;
    private final OffsetDateTime submittedAt;
    private CertificationStatus status;
    private OffsetDateTime updatedAt;

    public CertificationRecord(String requestId, String studentId, CertificationType type, String comment) {
        this.requestId = requestId;
        this.studentId = studentId;
        this.type = type;
        this.comment = comment;
        this.status = CertificationStatus.SUBMITTED;
        this.submittedAt = OffsetDateTime.now();
        this.updatedAt = this.submittedAt;
    }

    public String getRequestId() { return requestId; }
    public String getStudentId() { return studentId; }
    public CertificationType getType() { return type; }
    public String getComment() { return comment; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public CertificationStatus getStatus() { return status; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(CertificationStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }
}