package com.osozznanie.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "student_exams")
public class StudentMarkEntity implements Serializable {
	@EmbeddedId
	StudentMarkPK pk;

	@ManyToOne
	@MapsId("studentId")
	@JoinColumn(name = "student_id")
	private UserEntity student;

	@ManyToOne
	@MapsId("examId")
	@JoinColumn(name = "exam_id")
	private ExamEntity exam;

	private Integer mark;
}
