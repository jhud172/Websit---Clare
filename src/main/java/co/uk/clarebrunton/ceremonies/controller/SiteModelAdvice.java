package co.uk.clarebrunton.ceremonies.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.uk.clarebrunton.ceremonies.config.SiteProperties;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.service.SiteUrlResolver;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class SiteModelAdvice {

	private static final Logger logger = LoggerFactory.getLogger(SiteModelAdvice.class);
	private static final String DEFAULT_LOGO_PATH = "/images/brand/clc-wreath-logo.png";
	private static final String CLC_LOGO_PATH = "/images/brand/logo-clare-no-background+no-wreath-CLC.png";
	private static final String WREATH_LOGO_PATH = "/images/brand/logo-clare-no-background-wreath-only.png";
	private static final String HORIZONTAL_FEATHER_PATH = "/images/objects/feather-horizontal.png";
	private static final String VERTICAL_FEATHER_PATH = "/images/objects/feather-vertical.png";
	private static final String OPEN_GRAPH_IMAGE_PATH = "/images/brand/clc-wreath-logo.png";
	private static final String DEFAULT_INSTAGRAM_URL = "https://www.instagram.com/clareslifecelebrations/";
	private static final List<String> SERVICE_OPTIONS = List.of(
			"Wedding ceremony",
			"Funeral or memorial",
			"Naming ceremony",
			"Vow renewal",
			"Other ceremony"
	);

	private final SiteProperties siteProperties;
	private final SiteUrlResolver siteUrlResolver;

	private final ObjectMapper objectMapper = new ObjectMapper();

	public SiteModelAdvice(SiteProperties siteProperties, SiteUrlResolver siteUrlResolver) {
		this.siteProperties = siteProperties;
		this.siteUrlResolver = siteUrlResolver;
	}

	@ModelAttribute
	public void addSiteData(Model model, HttpServletRequest request) {
		String baseUrl = siteUrlResolver.resolvePublicBaseUrl(request, siteProperties.getBaseUrl());
		String canonicalUrl = baseUrl + normalisePath(request.getRequestURI());
		model.addAttribute("site", siteProperties);
		model.addAttribute("logoPath", DEFAULT_LOGO_PATH);
		model.addAttribute("clcLogoPath", CLC_LOGO_PATH);
		model.addAttribute("wreathLogoPath", WREATH_LOGO_PATH);
		model.addAttribute("horizontalFeatherPath", HORIZONTAL_FEATHER_PATH);
		model.addAttribute("verticalFeatherPath", VERTICAL_FEATHER_PATH);
		if (!StringUtils.hasText(siteProperties.getInstagramUrl())) {
			siteProperties.setInstagramUrl(DEFAULT_INSTAGRAM_URL);
		}
		model.addAttribute("canonicalUrl", canonicalUrl);
		model.addAttribute("openGraphImageUrl", baseUrl + OPEN_GRAPH_IMAGE_PATH);
		model.addAttribute("structuredDataJson", buildStructuredDataJson(baseUrl));
		if (!model.containsAttribute("serviceOptions")) {
			model.addAttribute("serviceOptions", SERVICE_OPTIONS);
		}
		if (!model.containsAttribute("inquiryForm")) {
			model.addAttribute("inquiryForm", new InquiryForm());
		}
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String handleUploadTooLarge(RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("attachmentError", "Files can be up to 5 MB each and 15 MB in total.");
		redirectAttributes.addFlashAttribute("openEnquiryModal", true);
		return "redirect:/";
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoResourceFoundException.class)
	public String handleNoResourceFound(NoResourceFoundException exception, Model model, HttpServletRequest request) {
		addSiteData(model, request);
		model.addAttribute("status", HttpStatus.NOT_FOUND.value());
		model.addAttribute("pageTitle", "Page not found");
		model.addAttribute("pageDescription", "The page you were looking for could not be found.");
		model.addAttribute("robotsContent", "noindex, nofollow");
		return "error";
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public String handleUnexpectedError(Exception exception, Model model, HttpServletRequest request) {
		logger.error("Unexpected error handled by global handler", exception);
		addSiteData(model, request);
		model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		model.addAttribute("pageTitle", "Something went wrong");
		model.addAttribute("pageDescription", "An unexpected error occurred.");
		model.addAttribute("robotsContent", "noindex, nofollow");
		return "error";
	}

	private String normalisePath(String requestUri) {
		if (!StringUtils.hasText(requestUri) || "/".equals(requestUri)) {
			return "/";
		}
		return requestUri.replaceAll("/+$", "");
	}

	private String buildStructuredDataJson(String baseUrl) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("@context", "https://schema.org");
		data.put("@type", "ProfessionalService");
		data.put("name", siteProperties.getName());
		data.put("url", baseUrl + "/");
		data.put("logo", baseUrl + DEFAULT_LOGO_PATH);
		data.put("image", baseUrl + OPEN_GRAPH_IMAGE_PATH);
		data.put("description", "Personal ceremonies for moments that matter, from joyful weddings to dignified celebrations of life across Durham and the North East.");
		data.put("areaServed", "Durham and the North East");

		if (StringUtils.hasText(siteProperties.getContactEmail())) {
			data.put("email", siteProperties.getContactEmail());
		}
		if (StringUtils.hasText(siteProperties.getPhoneNumber())) {
			data.put("telephone", siteProperties.getPhoneNumber());
		}
		if (StringUtils.hasText(siteProperties.getInstagramUrl())) {
			data.put("sameAs", new String[] { siteProperties.getInstagramUrl() });
		}

		try {
			return objectMapper.writeValueAsString(data);
		}
		catch (JsonProcessingException exception) {
			logger.warn("Could not build structured data JSON-LD", exception);
			return null;
		}
	}

}
