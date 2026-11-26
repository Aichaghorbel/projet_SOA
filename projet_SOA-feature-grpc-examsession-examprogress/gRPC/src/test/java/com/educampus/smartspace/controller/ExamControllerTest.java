package com.educampus.smartspace.controller;

import com.educampus.smartspace.exception.ExamSessionNotFoundException;
import com.educampus.smartspace.model.ExamSession;
import com.educampus.smartspace.model.ExamStatus;
import com.educampus.smartspace.model.Question;
import com.educampus.smartspace.model.QuestionType;
import com.educampus.smartspace.repository.ExamSessionRepository;
import com.educampus.smartspace.repository.QuestionRepository;
import com.educampus.smartspace.service.ExamService;
import com.educampus.smartspace.service.ExamService.ExamResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExamController.class)
@DisplayName("ExamController — REST API Tests")
class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExamService examService;

    @MockBean
    private ExamSessionRepository examSessionRepository;

    @MockBean
    private QuestionRepository questionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/exams/sessions/{id}: returns session when found")
    void testGetSessionSuccess() throws Exception {
        ExamSession session = ExamSession.builder()
                .id(1L)
                .examId(101L)
                .studentId(42L)
                .status(ExamStatus.NOT_STARTED)
                .maxScore(20.0)
                .startedAt(Instant.now())
                .build();

        when(examSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        mockMvc.perform(get("/api/v1/exams/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.studentId").value(42))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"));
    }

    @Test
    @DisplayName("GET /api/v1/exams/sessions/{id}: returns 404 when not found")
    void testGetSessionNotFound() throws Exception {
        when(examSessionRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/exams/sessions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/exams/{examId}/questions: returns list of questions")
    void testGetQuestions() throws Exception {
        Question q = Question.builder()
                .id(10L)
                .examId(101L)
                .questionNumber(1)
                .type(QuestionType.MCQ)
                .text("Test question")
                .maxPoints(5.0)
                .build();

        when(questionRepository.findByExamIdOrderByQuestionNumberAsc(101L)).thenReturn(List.of(q));

        mockMvc.perform(get("/api/v1/exams/101/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].type").value("MCQ"));
    }

    @Test
    @DisplayName("POST /api/v1/exams/sessions/{id}/submit: returns ExamResult")
    void testSubmitExamRest() throws Exception {
        ExamResult result = new ExamResult(1L, 18.0, 20.0, "A", true);
        when(examService.submitExam(eq(1L), eq(42L), eq(101L), anyList())).thenReturn(result);

        List<ExamService.StudentAnswerInput> body = List.of(
                new ExamService.StudentAnswerInput(10L, 100L, null)
        );

        mockMvc.perform(post("/api/v1/exams/sessions/1/submit")
                        .param("studentId", "42")
                        .param("examId", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.examSessionId").value(1))
                .andExpect(jsonPath("$.totalScore").value(18.0))
                .andExpect(jsonPath("$.grade").value("A"))
                .andExpect(jsonPath("$.passed").value(true));
    }
}
