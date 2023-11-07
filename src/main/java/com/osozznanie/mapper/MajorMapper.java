package com.osozznanie.mapper;

import com.osozznanie.domain.Major;
import com.osozznanie.domain.User;
import com.osozznanie.entity.MajorEntity;
import com.osozznanie.entity.StudentMajorEntity;
import com.osozznanie.entity.StudentMajorPK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MajorMapper implements Mapper<MajorEntity, Major> {
	private ExamMapper examMapper;
	private UserMapper userMapper;

	@Autowired
	public MajorMapper(ExamMapper examMapper, @Lazy UserMapper userMapper) {
		this.examMapper = examMapper;
		this.userMapper = userMapper;
	}

	@Override
	public MajorEntity mapDomainToEntity(Major major) {
		return major == null ? null :
				MajorEntity.builder()
						.id(major.getId())
						.title(major.getTitle())
						.capacity(major.getCapacity())
						.applicantsNum(major.getApplicantsNum())
						.examEntities(major.getExams().stream()
								.map(examMapper::mapDomainToEntity)
								.collect(Collectors.toList()))
						.build();
	}

	public MajorEntity mapDomainToEntityWithApplicants(Major major) {
		MajorEntity majorEntity = mapDomainToEntity(major);

		if (majorEntity != null) {
			majorEntity.setApplicants(major.getApplicants().stream()
					.map(applicant -> {
						StudentMajorEntity studMajor = new StudentMajorEntity();
						StudentMajorPK pk = new StudentMajorPK();
						pk.setStudentId(applicant.getId());
						pk.setMajorId(major.getId());
						studMajor.setPk(pk);
						studMajor.setStudent(userMapper.mapDomainToEntity(applicant));
						studMajor.setMajor(majorEntity);
						studMajor.setYouPassed(major.isYouPassed());
						return studMajor;
					}).collect(Collectors.toList()));
		}

		return majorEntity;
	}

	@Override
	public Major mapEntityToDomain(MajorEntity entity) {
		return entity == null ? null :
				Major.builder()
						.id(entity.getId())
						.title(entity.getTitle())
						.capacity(entity.getCapacity())
						.applicantsNum(entity.getApplicantsNum())
						.exams(entity.getExamEntities().stream()
								.map(examMapper::mapEntityToDomain)
								.collect(Collectors.toList()))
						.build();
	}

	public Major mapEntityToDomainWithApplicants(MajorEntity majorEntity) {
		Major major = mapEntityToDomain(majorEntity);

		if (major != null) {
			major.setApplicants(majorEntity.getApplicants().stream()
					.map(applicant -> {
						User student = userMapper.mapEntityToDomain(applicant.getStudent());
						major.setYouPassed(applicant.isYouPassed());
						major.setApplied(true);
						return student;
					}).collect(Collectors.toList()));
		}

		return major;
	}
}
