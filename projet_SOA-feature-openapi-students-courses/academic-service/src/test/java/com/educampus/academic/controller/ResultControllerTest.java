package com.educampus.academic.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.eq;
import com.educampus.academic.dto.PagedResponse;
import com.educampus.academic.dto.ResultRequest;
import com.educampus.academic.dto.ResultResponse;
import com.educampus.academic.entity.ResultSource;
import com.educampus.academic.entity.ResultStatus;
import com.educampus.academic.entity.SessionType;
import com.educampus.academic.exception.DuplicateResultException;
import com.educampus.academic.exception.ResultConflictException;
import com.educampus.academic.exception.ResultNotFoundException;
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

@WebMvcTest(ResultController.class)
class ResultControllerTest {

    private static final String CREATE_BODY =
            "{\"enrollmentId\":1,\"examId\":5,\"grade\":12.5,\"sessionType\":\"NORMAL\"}";

    private static final String UPDATE_BODY =
            "{\"grade\":15,\"sessionType\":\"RATTRAPAGE\",\"status\":\"PUBLISHED\",\"comment\":\"ok\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResultService service;

    private ResultResponse sample() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 10, 0);
        return new ResultResponse(100L, 1L, 5L, new BigDecimal("12.5"), SessionType.NORMAL,
                ResultStatus.DRAFT, ResultSource.MANUAL, "ok", now, null);
    }

    @Test
    void listReturns200() throws Exception {
        when(service.list(null, null, null, null, 0, 20))
                .thenReturn(new PagedResponse<>(List.of(sample()), 0, 20, 1L, 1));

        mockMvc.perform(get("/api/v1/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100));
    }

    @Test
    void listWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/results").param("status", "INCONNU"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReturns200() throws Exception {
        when(service.get(100L)).thenReturn(sample());

        mockMvc.perform(get("/api/v1/results/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value(12.5));
    }

    @Test
    void getUnknownReturns404() throws Exception {
        when(service.get(999L)).thenThrow(new ResultNotFoundException(999L));

        mockMvc.perform(get("/api/v1/results/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any(ResultRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/v1/results")
                        .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/results/100")))
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void createGradeAboveTwentyReturns400() throws Exception {
        String body = "{\"enrollmentId\":1,\"examId\":5,\"grade\":21,\"sessionType\":\"NORMAL\"}";

        mockMvc.perform(post("/api/v1/results")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createUnknownSessionTypeReturns400() throws Exception {
        String body = "{\"enrollmentId\":1,\"examId\":5,\"grade\":12,\"sessionType\":\"FOO\"}";

        mockMvc.perform(post("/api/v1/results")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createDuplicateReturns409() throws Exception {
        when(service.create(any(ResultRequest.class)))
                .thenThrow(new DuplicateResultException(1L, 5L, "NORMAL"));

        mockMvc.perform(post("/api/v1/results")
                        .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void createOnCancelledEnrollmentReturns409() throws Exception {
        when(service.create(any(ResultRequest.class)))
                .thenThrow(new ResultConflictException("L'inscription 1 est annulée"));

        mockMvc.perform(post("/api/v1/results")
                        .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(eq(100L), any())).thenReturn(sample());

        mockMvc.perform(put("/api/v1/results/100")
                        .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/results/100"))
                .andExpect(status().isNoContent());

        verify(service).delete(100L);
    }

    @Test
    void deletePublishedReturns409() throws Exception {
        doThrow(new ResultConflictException("Impossible de supprimer : il est publié"))
                .when(service).delete(100L);

        mockMvc.perform(delete("/api/v1/results/100"))
                .andExpect(status().isConflict());
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/results/abc"))
                .andExpect(status().isBadRequest());
    }
}