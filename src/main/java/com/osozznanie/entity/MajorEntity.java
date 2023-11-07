package com.osozznanie.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "major")
@Table(name = "majors")
public class MajorEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	@Column(name = "title")
	@NotEmpty
	private String title;
	@Column(name = "capacity")
	private int capacity;
	@Column(name = "applicants")
	private int applicantsNum;

	@ManyToMany
	@JoinTable(name = "major_exams", joinColumns = @JoinColumn(name = "major_id"),
			inverseJoinColumns = @JoinColumn(name = "exam_id"))
	@Cascade({CascadeType.MERGE, CascadeType.PERSIST})
	private List<ExamEntity> examEntities;
	@OneToMany(mappedBy = "major", cascade = javax.persistence.CascadeType.ALL)
	private List<StudentMajorEntity> applicants;
}
