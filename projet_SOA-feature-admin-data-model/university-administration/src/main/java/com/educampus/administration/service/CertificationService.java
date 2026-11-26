package com.educampus.administration.service;

import static com.educampus.administration.generated.CertificationStatus.*;

import com.educampus.administration.exception.*;
import com.educampus.administration.generated.CertificationStatus;
import com.educampus.administration.generated.CertificationType;
import com.educampus.administration.model.CertificationRecord;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class CertificationService {

    // Workflow : SUBMITTED -> IN_REVIEW -> APPROVED ou REJECTED
    private static final Map<CertificationStatus, Set<CertificationStatus>> TRANSITIONS = Map.of(
            SUBMITTED, Set.of(IN_REVIEW),
            IN_REVIEW, Set.of(APPROVED, REJECTED),
            APPROVED, Set.of(),
            REJECTED, Set.of());

    private final Map<String, CertificationRecord> store = new ConcurrentHashMap<>();

    public CertificationRecord submit(String studentId, CertificationType type, String comment) {
        if ("UNKNOWN".equalsIgnoreCase(studentId)) {
            throw new StudentNotFoundException(studentId);
        }
        boolean pending = store.values().stream().anyMatch(r ->
                r.getStudentId().equals(studentId)
                        && r.getType() == type
                        && (r.getStatus() == SUBMITTED || r.getStatus() == IN_REVIEW));
        if (pending) {
            throw new DuplicateCertificationException(studentId, type.value());
        }
        CertificationRecord record =
                new CertificationRecord(UUID.randomUUID().toString(), studentId, type, comment);
        store.put(record.getRequestId(), record);
        return record;
    }

    public CertificationRecord changeStatus(String requestId, CertificationStatus next) {
        CertificationRecord record = find(requestId);
        if (!TRANSITIONS.get(record.getStatus()).contains(next)) {
            throw new InvalidStatusTransitionException(record.getStatus().value(), next.value());
        }
        record.setStatus(next);
        return record;
    }

    public CertificationRecord find(String requestId) {
        CertificationRecord record = store.get(requestId);
        if (record == null) throw new CertificationNotFoundException(requestId);
        return record;
    }
}