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

import com.educampus.academic.dto.CourseRequest;
import com.educampus.academic.dto.CourseResponse;
import com.educampus.academic.dto.PageResponse;
import com.educampus.academic.entity.Semester;
import com.educampus.academic.exception.DuplicateResourceException;
import com.educampus.academic.exception.ResourceNotFoundException;
import com.educampus.academic.service.CourseService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    private static final String BODY = "{\"code\":\"SOA301\",\"title\":\"Architecture orientee services\","
            + "\"description\":\"REST, gRPC et SOAP\",\"credits\":4,\"semester\":\"S5\","
            + "\"teacherName\":\"Dr. Karim Trabelsi\",\"capacity\":40}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService service;

    private CourseResponse sample() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 10, 0);
        return new CourseResponse(1L, "SOA301", "Architecture orientee services",
                "REST, gRPC et SOAP", 4, Semester.S5, "Dr. Karim Trabelsi", 40, now, now);
    }

    @Test
    void listReturns200() throws Exception {
        when(service.list(0, 20, null, null))
                .thenReturn(new PageResponse<>(List.of(sample()), 0, 20, 1L, 1));

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("SOA301"));
    }

    @Test
    void listFilteredBySemesterReturns200() throws Exception {
        when(service.list(0, 20, Semester.S5, null))
                .thenReturn(new PageResponse<>(List.of(sample()), 0, 20, 1L, 1));

        mockMvc.perform(get("/api/v1/courses").param("semester", "S5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void unknownSemesterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/courses").param("semester", "S9"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getUnknownReturns404() throws Exception {
        when(service.get(42L)).thenThrow(new ResourceNotFoundException("Cours", 42L));

        mockMvc.perform(get("/api/v1/courses/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any(CourseRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/courses/1"));
    }

    @Test
    void createInvalidReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createDuplicateReturns409() throws Exception {
        when(service.create(any(CourseRequest.class)))
                .thenThrow(new DuplicateResourceException("Code deja utilise"));

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(eq(1L), any(CourseRequest.class))).thenReturn(sample());

        mockMvc.perform(put("/api/v1/courses/1")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/courses/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}
