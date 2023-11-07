package com.osozznanie.service;

import com.osozznanie.domain.Major;
import com.osozznanie.domain.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MajorsService {
	Page<Major> findAll(int pageIndex, int pageSize);
	Page<Major> findAllForStudent(User student,int pageIndex, int pageSize);
	Major applyForMajor(Integer majorId, User student);

	Major findById(Integer majorId);
	Major findByIdWithUserRanking(Integer majorId);
	Long count();

	Major save(Major major);
	void delete(Major major);

	List<User> admitApplicantsForMajor(Integer majorId);
	void resetAdmissionForMajor(Integer majorId);
	void admitApplicantsForAllMajors();
	void resetAdmissionForAllMajors();
}
