package com.osozznanie.mapper;

import com.osozznanie.domain.Exam;
import com.osozznanie.entity.ExamEntity;
import org.springframework.stereotype.Component;

@Component
public class ExamMapper implements Mapper<ExamEntity, Exam> {
	@Override
	public ExamEntity mapDomainToEntity(Exam exam) {
		return exam == null ? null :
				ExamEntity.builder()
						.id(exam.getId())
						.subject(exam.getSubject())
						.date(exam.getDate())
						.location(exam.getLocation())
						.build();
	}

	@Override
	public Exam mapEntityToDomain(ExamEntity entity) {
		return entity == null ? null :
				Exam.builder()
						.id(entity.getId())
						.subject(entity.getSubject())
						.date(entity.getDate())
						.location(entity.getLocation())
						.build();
	}
}
