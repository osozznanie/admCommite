package com.osozznanie.controller;

import com.osozznanie.domain.Exam;
import com.osozznanie.domain.Major;
import com.osozznanie.domain.PaginationUtility;
import com.osozznanie.domain.User;
import com.osozznanie.service.ExamService;
import com.osozznanie.service.MajorsService;
import com.osozznanie.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class StudentController {
	public static final String PAGE = "page";
	public static final String PAGE_SIZE = "pageSize";
	public static final String STUDENT = "student";
	public static final String SELECTED = "selected";

	private MajorsService majorsService;
	private ExamService examService;
	private UserService userService;
	private AuthenticationFacade authFacade;

	@ModelAttribute(name = STUDENT)
	public User provideStudentWithLists() {
		return userService.findById(authFacade.getPrincipalUser().getId());
	}

	@GetMapping({"/home"})
	public String home(@ModelAttribute(name = STUDENT) User student, Model model) {
		List<Major> majorsAdmittedTo = student.getMajors().stream()
				.filter(Major::isYouPassed).collect(Collectors.toList());
		model.addAttribute("majorsAdmittedTo", majorsAdmittedTo);
		return "student/student-home";
	}

	@GetMapping(value = {"/majors"})
	public ModelAndView listMajors(@RequestParam(value = PAGE_SIZE, required = false) String pageSizeStr,
								   @RequestParam(value = PAGE, required = false) String pageNumStr,
								   @RequestParam(required = false) Integer selected,
								   @ModelAttribute(name = STUDENT) User student) {

		final int pageSize = PaginationUtility.parsePageSize(pageSizeStr);
		final int pageIndex = PaginationUtility.parsePageNumber(pageNumStr) - 1;
		Page<Major> majorsPage = majorsService.findAllForStudent(student, pageIndex, pageSize);
		PaginationUtility pager = new PaginationUtility(majorsPage.getTotalPages(), majorsPage.getNumber());

		ModelAndView modelAndView = new ModelAndView("student/majors");
		modelAndView.addObject("majorsPage", majorsPage)
					.addObject("selectedPageSize", pageSize)
					.addObject("pageSizes", PaginationUtility.PAGE_SIZES)
					.addObject("pager", pager)
					.addObject(SELECTED, selected);

		return modelAndView;
	}

	@PostMapping(value = {"/majors/apply"})
	public ModelAndView applyForMajor(@RequestParam Integer majorId,
									  @RequestParam(value = PAGE_SIZE, required = false) String pageSizeStr,
									  @RequestParam(value = PAGE, required = false) String pageNumStr,
									  @ModelAttribute(name = STUDENT) User student) {

		majorsService.applyForMajor(majorId, student);

		ModelAndView modelAndView = new ModelAndView("redirect:/student/majors");
		modelAndView.addObject(PAGE_SIZE, pageSizeStr)
					.addObject(PAGE, pageNumStr)
					.addObject(SELECTED, majorId);

		return modelAndView;
	}

	@GetMapping(value = {"/majors/{majorId}/applicants"})
	public String showMajorApplicantsRanking(@PathVariable Integer majorId, Model model) {
		Major major = majorsService.findByIdWithUserRanking(majorId);
		model.addAttribute(major);
		return "student/major-applicants";
	}

	@PostMapping(value = {"/majors/exam-register"})
	public ModelAndView registerForExamInMajors(@RequestParam Integer examId,
												@RequestParam Integer majorId,
												@RequestParam(value = PAGE_SIZE, required = false) String pageSizeStr,
												@RequestParam(value = PAGE, required = false) String pageNumStr,
												@ModelAttribute(name = STUDENT) User student) {

		examService.registerStudent(examId, student);
		ModelAndView modelAndView = new ModelAndView("redirect:/student/majors");
		modelAndView.addObject(PAGE_SIZE, pageSizeStr)
					.addObject(PAGE, pageNumStr)
					.addObject(SELECTED, majorId);

		return modelAndView;
	}

	@GetMapping(value = {"/exams"})
	public String listExams(@ModelAttribute(name = STUDENT) User student, Model model) {
		List<Exam> examList = examService.findAllForStudent(student);
		model.addAttribute(examList);
		return "student/exams";
	}

	@PostMapping(value = {"/exams/register"})
	public String registerForExam(@RequestParam Integer examId,
								  @ModelAttribute(name = STUDENT) User student) {
		examService.registerStudent(examId, student);
		return "redirect:/student/exams";
	}
}
