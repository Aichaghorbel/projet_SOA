package com.educampus.academic.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.educampus.academic.dto.EnrollmentRequest;
import com.educampus.academic.dto.EnrollmentResponse;
import com.educampus.academic.dto.PagedResponse;
import com.educampus.academic.dto.ResultResponse;
import com.educampus.academic.entity.EnrollmentStatus;
import com.educampus.academic.entity.ResultSource;
import com.educampus.academic.entity.ResultStatus;
import com.educampus.academic.entity.SessionType;
import com.educampus.academic.exception.DuplicateEnrollmentException;
import com.educampus.academic.exception.EnrollmentConflictException;
import com.educampus.academic.exception.EnrollmentNotFoundException;
import com.educampus.academic.service.EnrollmentService;
import com.educampus.academic.service.ResultService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerTest {

    private static final String BODY =
            "{\"studentId\":10,\"courseId\":20,\"academicYear\":\"2026-2027\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private ResultService resultService;

    private EnrollmentResponse enrollment(EnrollmentStatus status) {
        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 10, 0);
        return new EnrollmentResponse(1L, 10L, 20L, "2026-2027", status, now, now);
    }

    private ResultResponse result() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 10, 0);
        return new ResultResponse(100L, 1L, 5L, new BigDecimal("12.5"), SessionType.NORMAL,
                ResultStatus.DRAFT, ResultSource.MANUAL, "ok", now, null);
    }

    @Test
    void listReturns200() throws Exception {
        when(enrollmentService.list(null, null, null, null, 0, 20))
                .thenReturn(new PagedResponse<>(List.of(enrollment(EnrollmentStatus.PENDING)), 0, 20, 1L, 1));

        mockMvc.perform(get("/api/v1/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getReturns200() throws Exception {
        when(enrollmentService.get(1L)).thenReturn(enrollment(EnrollmentStatus.PENDING));

        mockMvc.perform(get("/api/v1/enrollments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getUnknownReturns404() throws Exception {
        when(enrollmentService.get(42L)).thenThrow(new EnrollmentNotFoundException(42L));

        mockMvc.perform(get("/api/v1/enrollments/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(enrollmentService.create(any(EnrollmentRequest.class)))
                .thenReturn(enrollment(EnrollmentStatus.PENDING));

        mockMvc.perform(post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/enrollments/1")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createInvalidReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(enrollmentService);
    }

    @Test
    void createWrongAcademicYearReturns400() throws Exception {
        String body = "{\"studentId\":10,\"courseId\":20,\"academicYear\":\"2026\"}";

        mockMvc.perform(post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(enrollmentService);
    }

    @Test
    void createDuplicateReturns409() throws Exception {
        when(enrollmentService.create(any(EnrollmentRequest.class)))
                .thenThrow(new DuplicateEnrollmentException(10L, 20L, "2026-2027"));

        mockMvc.perform(post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createFullCourseReturns409() throws Exception {
        when(enrollmentService.create(any(EnrollmentRequest.class)))
                .thenThrow(new EnrollmentConflictException("Le cours INF101 est complet"));

        mockMvc.perform(post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void updateStatusReturns200() throws Exception {
        when(enrollmentService.updateStatus(1L, EnrollmentStatus.CONFIRMED))
                .thenReturn(enrollment(EnrollmentStatus.CONFIRMED));

        mockMvc.perform(patch("/api/v1/enrollments/1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updateStatusForbiddenReturns409() throws Exception {
        when(enrollmentService.updateStatus(1L, EnrollmentStatus.PENDING))
                .thenThrow(new EnrollmentConflictException("Transition interdite"));

        mockMvc.perform(patch("/api/v1/enrollments/1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PENDING\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/enrollments/1"))
                .andExpect(status().isNoContent());

        verify(enrollmentService).delete(1L);
    }

    @Test
    void deleteWithResultsReturns409() throws Exception {
        doThrow(new EnrollmentConflictException("des résultats y sont rattachés"))
                .when(enrollmentService).delete(1L);

        mockMvc.perform(delete("/api/v1/enrollments/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void listResultsReturns200() throws Exception {
        when(resultService.listByEnrollment(1L)).thenReturn(List.of(result()));

        mockMvc.perform(get("/api/v1/enrollments/1/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].grade").value(12.5));
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/enrollments/abc"))
                .andExpect(status().isBadRequest());
    }
}