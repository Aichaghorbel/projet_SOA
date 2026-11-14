package com.educampus.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.educampus.academic.dto.CourseRequest;
import com.educampus.academic.dto.CourseResponse;
import com.educampus.academic.dto.PageResponse;
import com.educampus.academic.entity.Course;
import com.educampus.academic.entity.Semester;
import com.educampus.academic.exception.DuplicateResourceException;
import com.educampus.academic.exception.ResourceNotFoundException;
import com.educampus.academic.repository.CourseRepository;
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
class CourseServiceTest {

    @Mock
    private CourseRepository repository;

    @InjectMocks
    private CourseService service;

    private CourseRequest request(String title) {
        return new CourseRequest("SOA301", title, "REST, gRPC et SOAP", 4, Semester.S5,
                "Dr. Karim Trabelsi", 40);
    }

    private Course existing(Long id) {
        Course c = new Course();
        c.setCode("SOA301");
        c.setTitle("Architecture orientee services");
        c.setCredits(4);
        c.setSemester(Semester.S5);
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    private PageImpl<Course> onePage() {
        return new PageImpl<>(List.of(existing(1L)), PageRequest.of(0, 20), 1);
    }

    @Test
    void createSavesCourse() {
        when(repository.existsByCode("SOA301")).thenReturn(false);
        when(repository.saveAndFlush(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 1L);
            return c;
        });

        CourseResponse response = service.create(request("Architecture orientee services"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.semester()).isEqualTo(Semester.S5);
    }

    @Test
    void createRejectsDuplicateCode() {
        when(repository.existsByCode("SOA301")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("Autre titre")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).saveAndFlush(any(Course.class));
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
        when(repository.existsByCodeAndIdNot("SOA301", 1L)).thenReturn(false);
        when(repository.saveAndFlush(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse response = service.update(1L, request("Nouveau titre"));

        assertThat(response.title()).isEqualTo("Nouveau titre");
    }

    @Test
    void updateRejectsCodeUsedByAnotherCourse() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing(1L)));
        when(repository.existsByCodeAndIdNot("SOA301", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request("Nouveau titre")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).saveAndFlush(any(Course.class));
    }

    @Test
    void deleteThrowsNotFound() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).delete(any(Course.class));
    }

    @Test
    void deleteRemovesCourse() {
        Course course = existing(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(course));

        service.delete(1L);

        verify(repository).delete(course);
        verify(repository).flush();
    }

    @Test
    void listWithoutFilterUsesFindAll() {
        when(repository.findAll(any(Pageable.class))).thenReturn(onePage());

        PageResponse<CourseResponse> result = service.list(0, 20, null, null);

        assertThat(result.content()).hasSize(1);
    }

    @Test
    void listWithSemesterOnlyUsesFindBySemester() {
        when(repository.findBySemester(eq(Semester.S5), any(Pageable.class))).thenReturn(onePage());

        assertThat(service.list(0, 20, Semester.S5, null).content()).hasSize(1);
    }

    @Test
    void listWithTextOnlyUsesSearch() {
        when(repository.search(eq("soa"), any(Pageable.class))).thenReturn(onePage());

        assertThat(service.list(0, 20, null, " soa ").content()).hasSize(1);
    }

    @Test
    void listWithSemesterAndTextUsesSearchBySemester() {
        when(repository.searchBySemester(eq(Semester.S5), eq("soa"), any(Pageable.class)))
                .thenReturn(onePage());

        assertThat(service.list(0, 20, Semester.S5, "soa").content()).hasSize(1);
        verify(repository, never()).search(anyString(), any(Pageable.class));
    }
}
