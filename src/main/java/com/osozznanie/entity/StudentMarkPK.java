package com.osozznanie.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;
import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "student_exams")
public class StudentMarkPK implements Serializable {
	@Column(name = "student_id")
	Integer studentId;

	@Column(name = "exam_id")
	Integer examId;
}
