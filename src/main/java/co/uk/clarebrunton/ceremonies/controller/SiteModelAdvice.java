package co.uk.clarebrunton.ceremonies.controller;

import co.uk.clarebrunton.ceremonies.config.SiteProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class SiteModelAdvice {

	private static final Logger logger = LoggerFactory.getLogger(SiteModelAdvice.class);
	private static final String DEFAULT_LOGO_PATH = "/images/brand/logo-clare.png";

	private final SiteProperties siteProperties;

	public SiteModelAdvice(SiteProperties siteProperties) {
		this.siteProperties = siteProperties;
	}

	@ModelAttribute
	public void addSiteData(Model model) {
		model.addAttribute("site", siteProperties);
		model.addAttribute("logoPath", DEFAULT_LOGO_PATH);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String handleUploadTooLarge(RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("attachmentError", "Files can be up to 5 MB each and 15 MB in total.");
		return "redirect:/contact";
	}

	@ExceptionHandler(Exception.class)
	public String handleUnexpectedError(Exception exception, Model model) {
		logger.error("Unexpected error handled by global handler", exception);
		model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		model.addAttribute("pageTitle", "Something went wrong");
		model.addAttribute("pageDescription", "An unexpected error occurred.");
		return "error";
	}

}
