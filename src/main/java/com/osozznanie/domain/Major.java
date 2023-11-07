package com.osozznanie.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Major {
	private Integer id;
	private String title;
	private List<Exam> exams;
	private int capacity;

	private List<User> applicants;
	private int applicantsNum;

	private boolean isApplied;
	private boolean youPassed;
}
