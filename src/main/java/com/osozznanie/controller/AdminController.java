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
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin")
@SessionAttributes({"user", "exam", "major", "batchAdmissionMsg"})
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AdminController {
	public static final String REDIRECT_ADMIN_MAJORS_EDIT = "redirect:/admin/majors/edit/";
	public static final String ADMIN_MAJOR_APPLICANTS = "admin/major-applicants";
	private boolean admitAllActivated;
	private boolean resetAdmissionActivated;

	private UserService userService;
	private MajorsService majorsService;
	private ExamService examService;
	private MessageSource messageSource;
	private AuthenticationFacade auth;


	@ModelAttribute(name = "batchAdmissionMsg")
	public String batchAdmissionMsg(Locale locale) {
		String batchAdmissionMsg = null;
		if (admitAllActivated) {
			batchAdmissionMsg = messageSource.getMessage("adm.msg.allAdmitted", new Object[]{}, locale);
		} else if (resetAdmissionActivated) {
			batchAdmissionMsg = messageSource.getMessage("adm.msg.allAdmissionsReset", new Object[]{}, locale);
		}
		return batchAdmissionMsg;
	}

	@GetMapping({"/home"})
	public String home(@ModelAttribute(name = "batchAdmissionMsg") String batchAdmissionMsg,
					   Model model, SessionStatus sessionStatus) {
		sessionStatus.setComplete();
		model.addAttribute("userCount", userService.count())
				.addAttribute("majorCount", majorsService.count())
				.addAttribute("examCount", examService.count());
		return "admin/admin-home";
	}

	@PostMapping({"/home/admit-all"})
	public String admitAll(SessionStatus sessionStatus) {
		sessionStatus.setComplete();
		majorsService.admitApplicantsForAllMajors();
		admitAllActivated = true;
		resetAdmissionActivated = false;
		log.info(String.format("Admin '%s' activated Admit All function.", auth.getPrincipalUser().getEmail()));
		return "redirect:/admin/home";
	}

	@PostMapping({"/home/reset-all"})
	public String resetAll(SessionStatus sessionStatus) {
		sessionStatus.setComplete();
		majorsService.resetAdmissionForAllMajors();
		resetAdmissionActivated = true;
		admitAllActivated = false;
		log.info(String.format("Admin '%s' activated Reset All function.", auth.getPrincipalUser().getEmail()));
		return "redirect:/admin/home";
	}

	@GetMapping(value = {"/users"})
	public ModelAndView listUsers(@RequestParam(value = "pageSize", required = false) String pageSizeStr,
								  @RequestParam(value = "page", required = false) String pageNumStr) {

		final int pageSize = PaginationUtility.parsePageSize(pageSizeStr);
		final int pageIndex = PaginationUtility.parsePageNumber(pageNumStr) - 1;
		Page<User> usersPage = userService.findAll(pageIndex, pageSize);
		PaginationUtility pager = new PaginationUtility(usersPage.getTotalPages(), usersPage.getNumber());

		ModelAndView modelAndView = new ModelAndView("admin/users");
		modelAndView.addObject("usersPage", usersPage)
					.addObject("selectedPageSize", pageSize)
					.addObject("pageSizes", PaginationUtility.PAGE_SIZES)
					.addObject("pager", pager);

		return modelAndView;
	}

	@GetMapping(value = {"/users/update/{userId}"})
	public ModelAndView showUpdateUserForm(@PathVariable Integer userId) {
		User user = userService.findById(userId);
		ModelAndView modelAndView = new ModelAndView("admin/user-update");
		modelAndView.addObject(user);
		return modelAndView;
	}

	@PostMapping(value = {"/users/update/submit"})
	public ModelAndView submitUserUpdate(@ModelAttribute User user, SessionStatus sessionStatus) {
		userService.saveStudentWithLists(user);
		sessionStatus.setComplete();
		return new ModelAndView("redirect:/admin/users/update/" + user.getId());
	}

	@GetMapping(value = {"/majors"})
	public ModelAndView listMajors(@RequestParam(value = "pageSize", required = false) String pageSizeStr,
								   @RequestParam(value = "page", required = false) String pageNumStr,
								   @RequestParam(required = false) Integer selected) {

		final int pageSize = PaginationUtility.parsePageSize(pageSizeStr);
		final int pageIndex = PaginationUtility.parsePageNumber(pageNumStr) - 1;
		Page<Major> majorsPage = majorsService.findAll(pageIndex, pageSize);
		PaginationUtility pager = new PaginationUtility(majorsPage.getTotalPages(), majorsPage.getNumber());

		ModelAndView modelAndView = new ModelAndView("admin/majors");
		modelAndView.addObject("majorsPage", majorsPage)
					.addObject("selectedPageSize", pageSize)
					.addObject("pageSizes", PaginationUtility.PAGE_SIZES)
					.addObject("pager", pager)
					.addObject("selected", selected);

		return modelAndView;
	}

	@GetMapping(value = {"/major/new"})
	public String showNewMajorForm(Model model, @ModelAttribute List<Exam> examList) {
		Major newMajor = new Major();
		newMajor.setExams(new ArrayList<>());
		model	.addAttribute("major", newMajor)
				.addAttribute(examList);
		return "admin/major-edit";
	}

	@GetMapping(value = {"/majors/edit/{majorId}"})
	public String showEditMajorForm(@PathVariable Integer majorId,
									@ModelAttribute List<Exam> examList,
									Model model) {
		Major major = majorsService.findById(majorId);
		model	.addAttribute(major)
				.addAttribute(examList);
		return "admin/major-edit";
	}

	@PostMapping(value = {"/majors/edit/add-exam"})
	public String addExamToMajor(@RequestParam Integer examId,
								 @ModelAttribute @Valid Major major,
								 @ModelAttribute List<Exam> examList) {
		Exam exam = examList.stream()
				.filter(e -> e.getId().equals(examId))
				.findFirst().orElse(null);
		major.getExams().add(exam);
		majorsService.save(major);
		return REDIRECT_ADMIN_MAJORS_EDIT + major.getId();
	}

	@PostMapping(value = {"/majors/edit/remove-exam"})
	public String removeExamFromMajor(@RequestParam int examIdx, @ModelAttribute @Valid Major major) {
		major.getExams().remove(examIdx);
		majorsService.save(major);
		return REDIRECT_ADMIN_MAJORS_EDIT + major.getId();
	}

	@PostMapping(value = {"/majors/save"})
	public String saveMajor(@ModelAttribute @Valid Major major, SessionStatus sessionStatus) {
		Major savedMajor = majorsService.save(major);
		if (major.getId() == null) {
			return REDIRECT_ADMIN_MAJORS_EDIT + savedMajor.getId();
		}
		sessionStatus.setComplete();
		return "redirect:/admin/majors";
	}

	@PostMapping(value = {"/majors/remove"})
	public String removeMajor(@ModelAttribute @Valid Major major, SessionStatus sessionStatus) {
		majorsService.delete(major);
		sessionStatus.setComplete();
		log.info(String.format("Admin '%s' removed the major '%s'",
				auth.getPrincipalUser().getEmail(), major.getTitle()));
		return "redirect:/admin/majors";
	}

	@GetMapping(value = {"/majors/{majorId}/applicants"})
	public String showMajorApplicantsRanking(@PathVariable Integer majorId, Model model) {
		Major major = majorsService.findByIdWithUserRanking(majorId);
		model.addAttribute(major);
		return ADMIN_MAJOR_APPLICANTS;
	}

	@ModelAttribute(name = "examList")
	public List<Exam> provideExamList() {
		return examService.findAll();
	}

	@GetMapping(value = {"/exams"})
	public String listExams(Model model, @ModelAttribute List<Exam> examList) {
		model.addAttribute(examList);
		return "admin/exams";
	}

	@GetMapping(value = {"/exams/new"})
	public String showNewExamForm(Model model) {
		model.addAttribute("exam", new Exam());
		return "admin/exam-edit";
	}

	@GetMapping(value = {"/exams/edit/{examId}"})
	public String showEditExamForm(@PathVariable Integer examId, Model model) {
		Exam exam = examService.findById(examId);
		model.addAttribute(exam);
		return "admin/exam-edit";
	}

	@PostMapping(value = {"/exams/save"})
	public String saveExam(@ModelAttribute @Valid Exam exam,
						   SessionStatus sessionStatus) {
		examService.save(exam);
		sessionStatus.setComplete();
		return "redirect:/admin/exams";
	}

	@PostMapping(value = {"/exams/remove"})
	public String removeExam(@ModelAttribute @Valid Exam exam,
							 SessionStatus sessionStatus) {
		examService.delete(exam);
		sessionStatus.setComplete();
		log.info(String.format("Admin '%s' removed the exam '%s'",
				auth.getPrincipalUser().getEmail(), exam.getSubject()));
		return "redirect:/admin/exams";
	}

	@PostMapping(value = {"/majors/{majorId}/applicants/admit"})
	public String admitStudentForMajor(@PathVariable Integer majorId, Model model, Locale locale) {
		majorsService.admitApplicantsForMajor(majorId);
		String admissionMsg = messageSource.getMessage("adm.msg.majorAdmitted", new Object[]{}, locale);
		model.addAttribute("admissionMsg", admissionMsg);
		admitAllActivated = false;
		resetAdmissionActivated = false;
		log.info(String.format("Admin '%s' admitted students to a major (ID %s)",
				auth.getPrincipalUser().getEmail(), majorId));
		return ADMIN_MAJOR_APPLICANTS;
	}

	@PostMapping(value = {"/majors/{majorId}/applicants/reset"})
	public String resetAdmissionForMajor(@PathVariable Integer majorId, Model model, Locale locale) {
		majorsService.resetAdmissionForMajor(majorId);
		String admissionMsg = messageSource.getMessage("adm.msg.majorAdmissionReset", new Object[]{}, locale);
		model.addAttribute("admissionMsg", admissionMsg);
		admitAllActivated = false;
		resetAdmissionActivated = false;
		log.info(String.format("Admin '%s' reset admission to a major (ID %s)",
				auth.getPrincipalUser().getEmail(), majorId));
		return ADMIN_MAJOR_APPLICANTS;
	}
}
