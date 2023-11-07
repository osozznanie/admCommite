package com.osozznanie.service.impl;

import com.osozznanie.domain.Exam;
import com.osozznanie.domain.User;
import com.osozznanie.entity.ExamEntity;
import com.osozznanie.exception.ExamNotFoundException;
import com.osozznanie.mapper.Mapper;
import com.osozznanie.repository.ExamRepository;
import com.osozznanie.service.ExamService;
import com.osozznanie.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ExamServiceImpl implements ExamService {
	private ExamRepository examRepository;
	private Mapper<ExamEntity, Exam> examMapper;
	private UserService userService;

	@Override
	public List<Exam> findAll() {
		return examRepository.findAll().stream()
				.map(examMapper::mapEntityToDomain)
				.collect(Collectors.toList());
	}

	@Override
	public List<Exam> findAllForStudent(User user) {
		List<Exam> userExams = user.getExams();
		List<Exam> allExams = findAll();

		Set<Integer> studentExamsIds = userExams.stream()
				.map(Exam::getId)
				.collect(Collectors.toSet());
		allExams.stream()
				.filter(exam -> !studentExamsIds.contains(exam.getId()))
				.forEach(userExams::add);

		return userExams;
	}

	@Override
	public Exam findById(Integer examId) {
		return examRepository.findById(examId)
				.map(examMapper::mapEntityToDomain)
				.orElseThrow(() -> new ExamNotFoundException("Exam with id [" + examId + "] was not found"));
	}

	@Override
	public Long count() {
		return examRepository.count();
	}

	@Override
	@Transactional
	public Exam registerStudent(Integer examId, User student) {
		Exam exam = findById(examId);

		Set<Integer> examIds = student.getExams().stream().map(Exam::getId).collect(Collectors.toSet());
		if (!examIds.contains(examId)) {
			student.getExams().add(exam);
			userService.saveStudentWithLists(student);
		}

		log.trace(String.format("Student '%s' registered for exam '%s'", student.getEmail(), exam.getSubject()));
		return exam;
	}

	@Override
	@Transactional
	public Exam save(Exam exam) {
		ExamEntity examEntity = examRepository.save(examMapper.mapDomainToEntity(exam));
		log.info(String.format("Exam '%s' ID '%d' was saved", exam.getSubject(), exam.getId()));
		return examMapper.mapEntityToDomain(examEntity);
	}

	@Override
	@Transactional
	public void delete(Exam exam) {
		examRepository.delete(examMapper.mapDomainToEntity(exam));
		log.info(String.format("Exam '%s' ID '%d' was deleted from the system", exam.getSubject(), exam.getId()));
	}
}
