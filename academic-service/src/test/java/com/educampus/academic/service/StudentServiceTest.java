package com.educampus.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.educampus.academic.dto.PageResponse;
import com.educampus.academic.dto.StudentRequest;
import com.educampus.academic.dto.StudentResponse;
import com.educampus.academic.entity.Student;
import com.educampus.academic.exception.DuplicateResourceException;
import com.educampus.academic.exception.ResourceNotFoundException;
import com.educampus.academic.repository.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository repository;

    @InjectMocks
    private StudentService service;

    private StudentRequest request(String firstName) {
        return new StudentRequest("ETU2026001", firstName, "Ben Salah", "amine@educampus.tn",
                LocalDate.of(2003, 5, 14), "+21650123456", "Genie Logiciel", 3);
    }

    private Student existing(Long id) {
        Student s = new Student();
        s.setStudentNumber("ETU2026001");
        s.setFirstName("Amine");
        s.setLastName("Ben Salah");
        s.setEmail("amine@educampus.tn");
        s.setLevel(3);
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }

    @Test
    void createSavesStudent() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByStudentNumber(anyString())).thenReturn(false);
        when(repository.saveAndFlush(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            ReflectionTestUtils.setField(s, "id", 1L);
            return s;
        });

        StudentResponse response = service.create(request("Amine"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("amine@educampus.tn");
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmail("amine@educampus.tn")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("Amine")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).saveAndFlush(any(Student.class));
    }

    @Test
    void createRejectsDuplicateStudentNumber() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByStudentNumber("ETU2026001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("Amine")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).saveAndFlush(any(Student.class));
    }

    @Test
    void getThrowsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void updateChangesFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing(1L)));
        when(repository.existsByEmailAndIdNot(anyString(), eq(1L))).thenReturn(false);
        when(repository.existsByStudentNumberAndIdNot(anyString(), eq(1L))).thenReturn(false);
        when(repository.saveAndFlush(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse response = service.update(1L, request("Sami"));

        assertThat(response.firstName()).isEqualTo("Sami");
    }

    @Test
    void updateRejectsEmailUsedByAnotherStudent() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing(1L)));
        when(repository.existsByEmailAndIdNot("amine@educampus.tn", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request("Sami")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).saveAndFlush(any(Student.class));
    }

    @Test
    void deleteThrowsNotFound() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).delete(any(Student.class));
    }

    @Test
    void deleteRemovesStudent() {
        Student student = existing(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(student));

        service.delete(1L);

        verify(repository).delete(student);
        verify(repository).flush();
    }

    @Test
    void listWithoutTextUsesFindAll() {
        var page = new PageImpl<>(List.of(existing(1L)), PageRequest.of(0, 20), 1);
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<StudentResponse> result = service.list(0, 20, "  ");

        assertThat(result.totalElements()).isEqualTo(1);
        verify(repository, never()).search(anyString(), any(Pageable.class));
    }

    @Test
    void listWithTextUsesSearch() {
        var page = new PageImpl<>(List.of(existing(1L)), PageRequest.of(0, 20), 1);
        when(repository.search(eq("ben"), any(Pageable.class))).thenReturn(page);

        PageResponse<StudentResponse> result = service.list(0, 20, " ben ");

        assertThat(result.content()).hasSize(1);
    }
}
