package com.osozznanie.service;

import com.osozznanie.domain.Exam;
import com.osozznanie.domain.User;

import java.util.List;

public interface ExamService {
	List<Exam> findAll();
	List<Exam> findAllForStudent(User user);

	Exam findById(Integer examId);
	Long count();

	Exam registerStudent(Integer examId, User student);
	Exam save(Exam exam);

	void delete(Exam exam);
}
