package com.educampus.administration.service;

import static org.junit.jupiter.api.Assertions.*;

import com.educampus.administration.exception.*;
import com.educampus.administration.generated.CertificationStatus;
import com.educampus.administration.generated.CertificationType;
import com.educampus.administration.model.CertificationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CertificationServiceTest {

    private CertificationService service;

    @BeforeEach
    void setUp() {
        service = new CertificationService();
    }

    @Test
    void submit_createsRequestWithStatusSubmitted() {
        CertificationRecord r = service.submit("STU-001", CertificationType.TRANSCRIPT, "bourse");
        assertNotNull(r.getRequestId());
        assertEquals(CertificationStatus.SUBMITTED, r.getStatus());
    }

    @Test
    void submit_unknownStudent_throwsFault() {
        assertThrows(StudentNotFoundException.class,
                () -> service.submit("UNKNOWN", CertificationType.TRANSCRIPT, null));
    }

    @Test
    void submit_duplicatePendingRequest_throws() {
        service.submit("STU-001", CertificationType.TRANSCRIPT, null);
        assertThrows(DuplicateCertificationException.class,
                () -> service.submit("STU-001", CertificationType.TRANSCRIPT, null));
    }

    @Test
    void submit_sameStudentOtherType_ok() {
        service.submit("STU-001", CertificationType.TRANSCRIPT, null);
        assertDoesNotThrow(() -> service.submit("STU-001", CertificationType.ENROLLMENT_CERTIFICATE, null));
    }

    @Test
    void workflow_submittedToInReviewToApproved() {
        String id = service.submit("STU-001", CertificationType.DEGREE_CERTIFICATE, null).getRequestId();
        assertEquals(CertificationStatus.IN_REVIEW, service.changeStatus(id, CertificationStatus.IN_REVIEW).getStatus());
        assertEquals(CertificationStatus.APPROVED, service.changeStatus(id, CertificationStatus.APPROVED).getStatus());
    }

    @Test
    void workflow_inReviewToRejected() {
        String id = service.submit("STU-001", CertificationType.DEGREE_CERTIFICATE, null).getRequestId();
        service.changeStatus(id, CertificationStatus.IN_REVIEW);
        assertEquals(CertificationStatus.REJECTED, service.changeStatus(id, CertificationStatus.REJECTED).getStatus());
    }

    @Test
    void workflow_cannotSkipInReview() {
        String id = service.submit("STU-001", CertificationType.TRANSCRIPT, null).getRequestId();
        assertThrows(InvalidStatusTransitionException.class,
                () -> service.changeStatus(id, CertificationStatus.APPROVED));
    }

    @Test
    void workflow_finalStatusCannotChange() {
        String id = service.submit("STU-001", CertificationType.TRANSCRIPT, null).getRequestId();
        service.changeStatus(id, CertificationStatus.IN_REVIEW);
        service.changeStatus(id, CertificationStatus.APPROVED);
        assertThrows(InvalidStatusTransitionException.class,
                () -> service.changeStatus(id, CertificationStatus.REJECTED));
    }

    @Test
    void changeStatus_unknownRequest_throws() {
        assertThrows(CertificationNotFoundException.class,
                () -> service.changeStatus("nope", CertificationStatus.IN_REVIEW));
    }

    @Test
    void afterRejection_studentCanResubmit() {
        String id = service.submit("STU-001", CertificationType.TRANSCRIPT, null).getRequestId();
        service.changeStatus(id, CertificationStatus.IN_REVIEW);
        service.changeStatus(id, CertificationStatus.REJECTED);
        assertDoesNotThrow(() -> service.submit("STU-001", CertificationType.TRANSCRIPT, null));
    }
}