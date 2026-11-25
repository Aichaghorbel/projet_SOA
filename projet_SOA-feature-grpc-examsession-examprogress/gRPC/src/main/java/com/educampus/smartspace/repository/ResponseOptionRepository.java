package com.educampus.smartspace.repository;

import com.educampus.smartspace.model.ResponseOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponseOptionRepository extends JpaRepository<ResponseOption, Long> {
    List<ResponseOption> findByQuestionId(Long questionId);
    List<ResponseOption> findByQuestionIdAndIsCorrectTrue(Long questionId);
    Optional<ResponseOption> findByQuestionIdAndOptionLetter(Long questionId, String optionLetter);
}
