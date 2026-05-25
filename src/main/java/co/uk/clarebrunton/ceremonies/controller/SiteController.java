package co.uk.clarebrunton.ceremonies.controller;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.uk.clarebrunton.ceremonies.config.ReviewProperties;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.model.ReviewForm;
import co.uk.clarebrunton.ceremonies.service.BlogService;
import co.uk.clarebrunton.ceremonies.service.InquiryNotificationService;
import co.uk.clarebrunton.ceremonies.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class SiteController {

	private static final String LOGO_CLARE = "/images/brand/logo-clare-no-background+no-wreath-CLC.png";

	private static final List<String> SERVICE_OPTIONS = List.of(
			"Wedding ceremony",
			"Funeral or memorial",
			"Naming ceremony",
			"Vow renewal",
			"Other ceremony"
	);
	private static final int MAX_ATTACHMENT_COUNT = 3;
	private static final long MAX_ATTACHMENT_SIZE_BYTES = 5L * 1024L * 1024L;
	private static final Set<String> ALLOWED_ATTACHMENT_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "pdf");
	private static final String REVIEW_ADMIN_SESSION_KEY = "reviewAdminAuthenticated";

	private final BlogService blogService;

	private final InquiryNotificationService inquiryNotificationService;

	private final ReviewService reviewService;

	private final ReviewProperties reviewProperties;

	public SiteController(BlogService blogService,
			InquiryNotificationService inquiryNotificationService,
			ReviewService reviewService,
			ReviewProperties reviewProperties) {
		this.blogService = blogService;
		this.inquiryNotificationService = inquiryNotificationService;
		this.reviewService = reviewService;
		this.reviewProperties = reviewProperties;
	}

	@ModelAttribute("serviceOptions")
	public List<String> serviceOptions() {
		return SERVICE_OPTIONS;
	}

	@ModelAttribute("inquiryForm")
	public InquiryForm inquiryForm() {
		return new InquiryForm();
	}

	@ModelAttribute("reviewCeremonyOptions")
	public List<String> reviewCeremonyOptions() {
		return SERVICE_OPTIONS;
	}

	@ModelAttribute("reviewForm")
	public ReviewForm reviewForm() {
		return new ReviewForm();
	}

	@GetMapping("/")
	public String home(Model model) {
		prepareHomeModel(model);
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Meet Clare Brunton");
		model.addAttribute("pageDescription", "Meet Clare Brunton, a North of England celebrant creating modern, personal ceremonies with warmth, compassion and calm confidence.");
		return "about";
	}

	@GetMapping("/services")
	public String services(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Ceremony services");
		model.addAttribute("pageDescription", "Wedding, funeral, naming, vow renewal and life ceremony services by Clare's Life Celebrations, created for moments that matter.");
		return "ceremonies";
	}

	@GetMapping("/weddings")
	public String weddings(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Wedding ceremonies in Durham");
		model.addAttribute("pageDescription", "Bespoke celebrant-led wedding ceremonies by Clare Riley Brunton, designed with freedom, warmth, personality and meaningful ceremony choices.");
		return "weddings";
	}

	@GetMapping("/funerals")
	public String funerals(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Funeral ceremonies in Durham");
		model.addAttribute("pageDescription", "Funeral, memorial and celebration of life ceremonies by Clare Riley Brunton, created with compassion, dignity and heartfelt personal tribute.");
		return "funerals";
	}

	@GetMapping("/reviews")
	public String reviews(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Client reviews");
		model.addAttribute("pageDescription", "Read approved reviews for Clare's Life Celebrations and share your own experience for moderation.");
		model.addAttribute("approvedReviews", reviewService.getApprovedReviews());
		return "reviews";
	}

	@PostMapping("/reviews/submit")
	public String submitReview(@Valid @ModelAttribute("reviewForm") ReviewForm reviewForm,
			BindingResult bindingResult,
			@RequestParam(name = "reviewPhotos", required = false) List<MultipartFile> reviewPhotos,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("logoPath", LOGO_CLARE);
			model.addAttribute("pageTitle", "Reviews");
			model.addAttribute("pageDescription", "Read approved reviews for Clare's Life Celebrations and share your own experience for moderation.");
			model.addAttribute("approvedReviews", reviewService.getApprovedReviews());
			return "reviews";
		}

		try {
			reviewService.submitReview(reviewForm, reviewPhotos);
		}
		catch (IllegalArgumentException exception) {
			model.addAttribute("logoPath", LOGO_CLARE);
			model.addAttribute("pageTitle", "Reviews");
			model.addAttribute("pageDescription", "Read approved reviews for Clare's Life Celebrations and share your own experience for moderation.");
			model.addAttribute("approvedReviews", reviewService.getApprovedReviews());
			model.addAttribute("reviewUploadError", exception.getMessage());
			return "reviews";
		}

		redirectAttributes.addFlashAttribute("reviewSubmissionSuccess", "Thank you. Your review has been received and is now pending approval.");
		return "redirect:/reviews";
	}

	@PostMapping(value = "/reviews/submit", headers = "X-Requested-With=XMLHttpRequest")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> submitReviewAjax(@Valid @ModelAttribute("reviewForm") ReviewForm reviewForm,
			BindingResult bindingResult,
			@RequestParam(name = "reviewPhotos", required = false) List<MultipartFile> reviewPhotos) {
		if (bindingResult.hasErrors()) {
			Map<String, String> errors = new LinkedHashMap<>();
			bindingResult.getFieldErrors().forEach((error) -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", false);
			body.put("message", "Please check the highlighted fields.");
			body.put("errors", errors);
			return ResponseEntity.badRequest().body(body);
		}

		try {
			reviewService.submitReview(reviewForm, reviewPhotos);
		}
		catch (IllegalArgumentException exception) {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", false);
			body.put("message", exception.getMessage());
			return ResponseEntity.badRequest().body(body);
		}

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("message", "Thank you. Your review has been received and is now pending approval.");
		return ResponseEntity.ok(body);
	}

	@GetMapping("/reviews/admin/login")
	public String reviewAdminLogin(Model model, HttpSession session) {
		if (isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin";
		}

		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Review admin login");
		model.addAttribute("pageDescription", "Admin login for review moderation.");
		model.addAttribute("robotsContent", "noindex, nofollow");
		return "reviews-admin-login";
	}

	@PostMapping("/reviews/admin/login")
	public String submitReviewAdminLogin(@RequestParam("username") String username,
			@RequestParam("password") String password,
			RedirectAttributes redirectAttributes,
			HttpSession session) {
		if (reviewProperties.getAdminUsername().equals(username) && reviewProperties.getAdminPassword().equals(password)) {
			session.setAttribute(REVIEW_ADMIN_SESSION_KEY, Boolean.TRUE);
			return "redirect:/reviews/admin";
		}

		redirectAttributes.addFlashAttribute("reviewAdminError", "Login details were not recognised.");
		return "redirect:/reviews/admin/login";
	}

	@PostMapping("/reviews/admin/logout")
	public String logoutReviewAdmin(HttpSession session) {
		session.removeAttribute(REVIEW_ADMIN_SESSION_KEY);
		return "redirect:/reviews/admin/login";
	}

	@GetMapping("/reviews/admin")
	public String reviewAdmin(Model model, HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Review moderation");
		model.addAttribute("pageDescription", "Approve or reject submitted reviews.");
		model.addAttribute("robotsContent", "noindex, nofollow");
		model.addAttribute("pendingReviews", reviewService.getPendingReviews());
		return "reviews-admin";
	}

	@GetMapping("/reviews/admin/manage")
	public String manageReviews(Model model, HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Manage reviews");
		model.addAttribute("pageDescription", "Enable or disable approved reviews.");
		model.addAttribute("robotsContent", "noindex, nofollow");
		model.addAttribute("managedReviews", reviewService.getManageableReviews());
		return "reviews-admin-manage";
	}

	@PostMapping("/reviews/admin/{reviewId}/approve")
	public String approveReview(@PathVariable String reviewId,
			@RequestParam(name = "note", required = false) String note,
			RedirectAttributes redirectAttributes,
			HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		reviewService.approveReview(reviewId, note);
		redirectAttributes.addFlashAttribute("reviewAdminMessage", "Review approved.");
		return "redirect:/reviews/admin";
	}

	@PostMapping("/reviews/admin/{reviewId}/reject")
	public String rejectReview(@PathVariable String reviewId,
			@RequestParam(name = "note", required = false) String note,
			RedirectAttributes redirectAttributes,
			HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		reviewService.rejectReview(reviewId, note);
		redirectAttributes.addFlashAttribute("reviewAdminMessage", "Review rejected and deleted.");
		return "redirect:/reviews/admin";
	}

	@PostMapping("/reviews/admin/{reviewId}/enable")
	public String enableReview(@PathVariable String reviewId,
			RedirectAttributes redirectAttributes,
			HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		reviewService.enableReview(reviewId);
		redirectAttributes.addFlashAttribute("reviewAdminMessage", "Review enabled.");
		return "redirect:/reviews/admin/manage";
	}

	@PostMapping("/reviews/admin/{reviewId}/disable")
	public String disableReview(@PathVariable String reviewId,
			RedirectAttributes redirectAttributes,
			HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		reviewService.disableReview(reviewId);
		redirectAttributes.addFlashAttribute("reviewAdminMessage", "Review disabled.");
		return "redirect:/reviews/admin/manage";
	}

	@GetMapping("/review-photos/{filename}")
	@ResponseBody
	public ResponseEntity<Resource> reviewPhoto(@PathVariable String filename) {
		Path resourcePath = reviewService.resolvePhotoPath(filename);
		Resource resource = new FileSystemResource(resourcePath);

		if (!resource.exists()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
		String extension = StringUtils.getFilenameExtension(filename);

		if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) {
			mediaType = MediaType.IMAGE_JPEG;
		}
		else if ("png".equalsIgnoreCase(extension)) {
			mediaType = MediaType.IMAGE_PNG;
		}
		else if ("webp".equalsIgnoreCase(extension)) {
			mediaType = MediaType.parseMediaType("image/webp");
		}

		return ResponseEntity.ok().contentType(mediaType).body(resource);
	}

	@GetMapping("/ceremonies")
	public String ceremoniesRedirect() {
		return "redirect:/services";
	}

	@GetMapping("/blog")
	public String blog(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Journal");
		model.addAttribute("pageDescription", "Journal notes on celebrant-led weddings, funeral ceremonies and planning meaningful moments.");
		model.addAttribute("posts", blogService.findAll());
		return "blog";
	}

	@GetMapping("/blog/{slug}")
	public String blogPost(@PathVariable String slug, Model model) {
		var post = blogService.findBySlug(slug);
		if (post == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", post.title());
		model.addAttribute("pageDescription", post.excerpt());
		model.addAttribute("post", post);
		model.addAttribute("posts", blogService.findAll());
		return "blog-post";
	}

	@GetMapping("/contact")
	public String contact(Model model) {
		return "redirect:/";
	}

	@PostMapping("/contact")
	public String submitContact(@Valid @ModelAttribute("inquiryForm") InquiryForm inquiryForm,
			BindingResult bindingResult,
			@RequestParam(name = "attachments", required = false) List<MultipartFile> attachments,
			Model model,
			RedirectAttributes redirectAttributes) {
		List<MultipartFile> uploadedAttachments = normaliseAttachments(attachments);
		String attachmentError = validateAttachments(uploadedAttachments);

		if (bindingResult.hasErrors() || attachmentError != null) {
			prepareHomeModel(model);
			model.addAttribute("openEnquiryModal", true);
			if (attachmentError != null) {
				model.addAttribute("attachmentError", attachmentError);
			}
			return "home";
		}

		if (uploadedAttachments.isEmpty()) {
			inquiryNotificationService.handleInquiry(inquiryForm);
		}
		else {
			inquiryNotificationService.handleInquiry(inquiryForm, uploadedAttachments);
		}
		redirectAttributes.addFlashAttribute("submittedServiceType", inquiryForm.getServiceType());
		return "redirect:/thank-you";
	}

	public String submitContact(InquiryForm inquiryForm,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {
		return submitContact(inquiryForm, bindingResult, List.of(), model, redirectAttributes);
	}

	@GetMapping("/thank-you")
	public String thankYou(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Thank you for getting in touch");
		model.addAttribute("pageDescription", "Confirmation page after submitting an enquiry to Clare's Life Celebrations.");
		model.addAttribute("robotsContent", "noindex, nofollow");
		return "thank-you";
	}

	@GetMapping("/privacy")
	public String privacy(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Privacy Policy");
		model.addAttribute("pageDescription", "How Clare's Life Celebrations collects, stores and uses personal information.");
		return "privacy";
	}

	private List<MultipartFile> normaliseAttachments(List<MultipartFile> attachments) {
		if (attachments == null) {
			return List.of();
		}
		return attachments.stream()
				.filter(file -> file != null && !file.isEmpty())
				.toList();
	}

	private void prepareHomeModel(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Clare's Life Celebrations | Weddings and funerals in Durham");
		model.addAttribute("pageDescription", "Clare Riley Brunton creates personal ceremonies for moments that matter, from joyful weddings to dignified celebrations of life across Durham and the North East.");
		model.addAttribute("featuredReviews", reviewService.getApprovedFiveStarReviews());
	}

	private String validateAttachments(List<MultipartFile> attachments) {
		if (attachments.size() > MAX_ATTACHMENT_COUNT) {
			return "Please upload up to 3 files only.";
		}

		for (MultipartFile attachment : attachments) {
			if (attachment.getSize() > MAX_ATTACHMENT_SIZE_BYTES) {
				return "Each file needs to be 5 MB or smaller.";
			}

			String extension = StringUtils.getFilenameExtension(attachment.getOriginalFilename());
			if (!StringUtils.hasText(extension) || !ALLOWED_ATTACHMENT_EXTENSIONS.contains(extension.toLowerCase())) {
				return "Please use JPG, PNG, WEBP or PDF files only.";
			}
		}

		return null;
	}

	private boolean isReviewAdminAuthenticated(HttpSession session) {
		Object value = session.getAttribute(REVIEW_ADMIN_SESSION_KEY);
		return value instanceof Boolean authenticated && authenticated;
	}

}
