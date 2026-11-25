package com.educampus.academic.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.educampus.academic.dto.PageResponse;
import com.educampus.academic.dto.StudentRequest;
import com.educampus.academic.dto.StudentResponse;
import com.educampus.academic.exception.DuplicateResourceException;
import com.educampus.academic.exception.ResourceNotFoundException;
import com.educampus.academic.service.StudentService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    private static final String BODY = "{\"studentNumber\":\"ETU2026001\",\"firstName\":\"Amine\","
            + "\"lastName\":\"Ben Salah\",\"email\":\"amine.bensalah@educampus.tn\","
            + "\"dateOfBirth\":\"2003-05-14\",\"phone\":\"+21650123456\","
            + "\"program\":\"Genie Logiciel\",\"level\":3}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService service;

    private StudentResponse sample() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 10, 0);
        return new StudentResponse(1L, "ETU2026001", "Amine", "Ben Salah",
                "amine.bensalah@educampus.tn", LocalDate.of(2003, 5, 14), "+21650123456",
                "Genie Logiciel", 3, now, now);
    }

    @Test
    void listReturns200() throws Exception {
        when(service.list(0, 20, null))
                .thenReturn(new PageResponse<>(List.of(sample()), 0, 20, 1L, 1));

        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getReturns200() throws Exception {
        when(service.get(1L)).thenReturn(sample());

        mockMvc.perform(get("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentNumber").value("ETU2026001"));
    }

    @Test
    void getUnknownReturns404() throws Exception {
        when(service.get(42L)).thenThrow(new ResourceNotFoundException("Etudiant", 42L));

        mockMvc.perform(get("/api/v1/students/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any(StudentRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/students/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createInvalidReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void createDuplicateReturns409() throws Exception {
        when(service.create(any(StudentRequest.class)))
                .thenThrow(new DuplicateResourceException("Email deja utilise"));

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(eq(1L), any(StudentRequest.class))).thenReturn(sample());

        mockMvc.perform(put("/api/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/students/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/students/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sizeAboveLimitReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/students").param("size", "1000"))
                .andExpect(status().isBadRequest());
    }
}
