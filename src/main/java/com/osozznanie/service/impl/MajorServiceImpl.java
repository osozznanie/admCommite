package com.osozznanie.service.impl;

import com.osozznanie.domain.Exam;
import com.osozznanie.domain.Major;
import com.osozznanie.domain.PaginationUtility;
import com.osozznanie.domain.User;
import com.osozznanie.entity.MajorEntity;
import com.osozznanie.exception.MajorNotFoundException;
import com.osozznanie.mapper.MajorMapper;
import com.osozznanie.repository.MajorRepository;
import com.osozznanie.service.MajorsService;
import com.osozznanie.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MajorServiceImpl implements MajorsService {
	private MajorRepository majorRepository;
	private MajorMapper majorMapper;
	private UserService userService;

	@Override
	public Page<Major> findAll(int pageIndex, int pageSize) {
		pageIndex = PaginationUtility.limitPageIndex(majorRepository.count(), pageIndex, pageSize);
		return majorRepository.findAll(PageRequest.of(pageIndex, pageSize))
				.map(majorMapper::mapEntityToDomain);
	}

	@Override
	public Page<Major> findAllForStudent(User student, int pageIndex, int pageSize) {
		Page<Major> majorPage = findAll(pageIndex, pageSize);
		List<Major> majors = majorPage.toList();

		Map<Integer, Major> studentMajors = student.getMajors().stream()
				.collect(Collectors.toMap(Major::getId, major -> major));
		Map<Integer, Exam> studentExams = student.getExams().stream()
				.collect(Collectors.toMap(Exam::getId, exam -> exam));

		majors.stream()
				.filter(major -> studentMajors.containsKey(major.getId()))
				.forEach(major -> major.setYouPassed(true));

		majors.forEach(major -> major.getExams().stream()
				.filter(exam -> studentExams.containsKey(exam.getId()))
				.forEach(exam -> {
					exam.setRegistered(true);
					exam.setMark(studentExams.get(exam.getId()).getMark());
				}));

		return majorPage;
	}

	@Override
	public Major findById(Integer majorId) {
		return majorRepository.findById(majorId)
				.map(majorMapper::mapEntityToDomainWithApplicants)
				.orElseThrow(() -> new MajorNotFoundException("Major with ID [" + majorId + "] was not found"));
	}

	@Override
	public Long count() {
		return majorRepository.count();
	}

	@Override
	@Transactional
	public Major save(Major major) {
		MajorEntity majorEntity = majorRepository.save(majorMapper.mapDomainToEntity(major));
		log.info(String.format("Major '%s' ID '%d' was saved", major.getTitle(), major.getId()));
		return majorMapper.mapEntityToDomain(majorEntity);
	}

	@Override
	@Transactional
	public void delete(Major major) {
		majorRepository.delete(majorMapper.mapDomainToEntity(major));
		log.info(String.format("Major '%s' ID '%d' was deleted from the system", major.getTitle(), major.getId()));
	}

	@Override
	@Transactional
	public Major applyForMajor(Integer majorId, User student) {
		MajorEntity majorEntity = majorRepository.findById(majorId)
				.orElseThrow(() -> new MajorNotFoundException("Major with id [" + majorId + "] is not found"));
		majorEntity.setApplicantsNum(majorEntity.getApplicantsNum() + 1);
		majorEntity = majorRepository.save(majorEntity);

		Set<Integer> majorIds = student.getMajors().stream().map(Major::getId).collect(Collectors.toSet());
		if (!majorIds.contains(majorId)) {
			student.getMajors().add(majorMapper.mapEntityToDomain(majorEntity));
			userService.saveStudentWithLists(student);
		}

		log.debug(String.format("Student '%s' (ID %d) applied for major '%s' (ID %d)",
				student.getEmail(), student.getId(), majorEntity.getTitle(), majorEntity.getId()));
		return majorMapper.mapEntityToDomain(majorEntity);
	}

	@Override
	public Major findByIdWithUserRanking(Integer majorId) {
		Major major = findById(majorId);
		Set<Integer> majorExamsIds = major.getExams().stream()
				.map(Exam::getId)
				.collect(Collectors.toSet());

		List<User> applicants = major.getApplicants().stream()
				.map(student -> student = userService.findById(student.getId()))
				.collect(Collectors.toList());

		applicants.forEach(student -> student.getExams().forEach(exam -> {
			if (majorExamsIds.contains(exam.getId())) {
				student.setMajorScore(student.getMajorScore() + exam.getMark());
			}
		}));

		applicants.sort(Comparator.comparing(User::getMajorScore).reversed());

		major.setApplicants(applicants);
		return major;
	}

	@Override
	public List<User> admitApplicantsForMajor(Integer majorId) {
		Major major = findByIdWithUserRanking(majorId);
		List<User> admittedStudents = new ArrayList<>();
		major.getApplicants().stream().limit(major.getCapacity()).forEach(student -> {
			student.getMajors().stream()
					.filter(m -> m.getId().equals(majorId))
					.findFirst()
					.orElseGet(() -> {
						student.getMajors().add(major);
						return major;
					}).setYouPassed(true);
			userService.saveStudentWithLists(student);
			admittedStudents.add(student);
		});
		log.debug(String.format("'%d' students were admitted for major '%s' (ID %d)",
				admittedStudents.size(), major.getTitle(), major.getId()));
		return admittedStudents;
	}

	@Override
	public void resetAdmissionForMajor(Integer majorId) {
		Major major = findByIdWithUserRanking(majorId);
		major.getApplicants().forEach(student -> {
			student.getMajors().stream()
					.filter(m -> m.getId().equals(majorId))
					.findFirst()
					.orElseGet(() -> {
						student.getMajors().add(major);
						return major;
					}).setYouPassed(false);
			userService.saveStudentWithLists(student);
		});
		log.debug(String.format("Admission was reset for major '%s' (ID %d)", major.getTitle(), major.getId()));
	}

	@Override
	public void admitApplicantsForAllMajors() {
		majorRepository.findAll().forEach(major -> admitApplicantsForMajor(major.getId()));
	}

	@Override
	public void resetAdmissionForAllMajors() {
		majorRepository.findAll().forEach(major -> resetAdmissionForMajor(major.getId()));
	}
}
