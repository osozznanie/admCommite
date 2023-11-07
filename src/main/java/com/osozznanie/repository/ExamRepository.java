package com.osozznanie.repository;

import com.osozznanie.entity.ExamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<ExamEntity, Integer> {
}
