package com.osozznanie.controller;

import com.osozznanie.domain.Role;
import com.osozznanie.domain.User;
import com.osozznanie.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Set;

@Controller
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
	private AuthenticationFacade authFacade;
	private UserService userService;

	@GetMapping({"", "/"})
	public String index() {
		Authentication authentication = authFacade.getAuthentication();
		Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
		if (!authentication.isAuthenticated() || roles.contains("ROLE_ANONYMOUS")) {
			return "index";
		}
		return authFacade.getPrincipalUser().getRole().equals(Role.STUDENT) ?
				"redirect:/student/home" : "redirect:/admin/home";
	}

	@GetMapping(value = {"/login"})
	public String logIn() {
		return "login";
	}

	@PostMapping("/login-success")
	public String logInSuccess() {
		return "redirect:";
	}

	@GetMapping(value = {"/register"})
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@PostMapping(value = {"/register"})
	public String submitRegisterForm(@ModelAttribute @Valid User user) {
		user.setRole(Role.STUDENT);
		userService.register(user);
		return "redirect:/login";
	}

	@GetMapping(value = {"/profile"})
	public String showProfileForm(Model model) {
		User currentUser = userService.findById(authFacade.getPrincipalUser().getId());
		model.addAttribute("userUpdate", currentUser);
		return "profile";
	}

	@PostMapping(value = {"/profile"})
	public String submitProfileForm(@ModelAttribute @Valid User userUpdate,
									HttpServletRequest request,
									HttpServletResponse response) {
		User currentUser = authFacade.getPrincipalUser();
		userService.updateProfile(currentUser, userUpdate);
		new SecurityContextLogoutHandler().logout(request, response, authFacade.getAuthentication());
		return "redirect:/login";
	}
}