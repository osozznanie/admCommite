package com.osozznanie.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleGenericException(final Throwable throwable, final Model model, HttpServletRequest request) {
		final String url = request.getRequestURL().toString();
		log.error("Exception on requested URL: '" + url + "'", throwable);

		final String errorMessage = (throwable != null ? throwable.getMessage() : "Unknown error");

		model	.addAttribute("errorMessage", errorMessage)
				.addAttribute("requestUrl", url);
		return "error/error";
	}

}
