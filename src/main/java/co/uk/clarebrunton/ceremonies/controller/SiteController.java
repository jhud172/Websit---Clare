package co.uk.clarebrunton.ceremonies.controller;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.uk.clarebrunton.ceremonies.config.ReviewProperties;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.model.ReviewEntry;
import co.uk.clarebrunton.ceremonies.model.ReviewForm;
import co.uk.clarebrunton.ceremonies.service.AnalyticsService;
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
			"Celebration of Life or memorial",
			"Naming ceremony",
			"Vow renewal",
			"Other ceremony"
	);
	private static final int MAX_ATTACHMENT_COUNT = 3;
	private static final long MAX_ATTACHMENT_SIZE_BYTES = 5L * 1024L * 1024L;
	private static final Set<String> ALLOWED_ATTACHMENT_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "pdf");
	private static final String REVIEW_ADMIN_SESSION_KEY = "reviewAdminAuthenticated";

	private static final List<Map<String, String>> SERVICE_FAQS = List.of(
			faq(
					"What ceremonies does Clare's Life Celebrations offer?",
					"Clare creates personal wedding ceremonies, celebrations of life and memorial ceremonies, naming ceremonies, vow renewals and other meaningful life celebrations. Each ceremony is shaped around the people, story, tone and moment involved."
			),
			faq(
					"Where does Clare work?",
					"Clare is based in Durham and supports ceremonies across the North East and beyond. Travel, venue details and any extra costs are discussed clearly during your enquiry."
			),
			faq(
					"Can Clare legally marry us?",
					"As the law currently stands in England and Wales, Clare's independent celebrant-led ceremony is not legally binding by itself. Couples choosing the civil route complete the legal marriage with a registrar, while Clare creates and leads the personal ceremony around your story, vows, readings, music and symbolic details."
			),
			faq(
					"How does the ceremony process work?",
					"The process starts with a conversation about your date, location, ceremony type and the atmosphere you want. Clare then listens carefully, shapes the tone with you, writes the ceremony and delivers it with calm, confident presence on the day."
			),
			faq(
					"How much does a ceremony cost?",
					"Wedding packages are £725, £925 and £1,195. Naming Ceremony packages are £395 and £545, Vow Renewal packages are £695 and £895, and Celebration of Life packages are £395 and £595. Concise venue farewells are typically £275 to £350. Optional extras and additional travel may affect the final total."
			),
			faq(
					"How do I enquire about availability?",
					"Use the enquiry form to share the ceremony type, preferred date, location and anything Clare should know at this stage. Clare will then guide you towards the best next step."
			)
	);

	private static final List<Map<String, String>> FAQ_PAGE_ENTRIES = List.of(
			faq(
					"What does a celebrant do?",
					"A celebrant creates and leads personalised ceremonies for weddings, vow renewals, naming ceremonies, celebrations of life and other meaningful life events."
			),
			faq(
					"Do we need a registrar as well as a celebrant?",
					"A registrar conducts or attends the civil ceremony that fulfils the legal marriage requirements. Clare creates and leads a personalised, non-legally binding ceremony. Many couples choose both and arrange them for the same day or separate dates."
			),
			faq(
					"How much does a celebrant cost?",
					"Clare's current packages range from £395 for a Naming Ceremony to £1,195 for the Complete Ceremony Experience. The service pages show each package, its inclusions and optional extras, and Clare will confirm the final total for your plans before booking."
			),
			faq(
					"How far in advance should I book a celebrant?",
					"Booking 12 to 18 months ahead is recommended, especially for popular spring and summer weekend dates."
			),
			faq(
					"Can a celebrant legally marry us in the UK?",
					"As the law currently stands in England and Wales, Clare's independent celebrant-led ceremony is not legally binding by itself. Couples choosing the civil route complete the legal marriage separately with a registrar."
			),
			faq(
					"What happens at a celebrant wedding ceremony?",
					"A celebrant wedding ceremony is built around your story and can include personal vows, readings, music, symbolic rituals and family involvement."
			),
			faq(
					"Can we write our own vows?",
					"Yes, couples are encouraged to write personal vows, and celebrant guidance can be provided to help shape them with confidence."
			),
			faq(
					"How long does a celebrant-led ceremony last?",
					"Most celebrant-led ceremonies run for around 20 to 45 minutes, depending on readings, rituals, speakers and personal elements."
			),
			faq(
					"Can we include family members, children or pets?",
					"Yes, celebrant ceremonies can include family, children, friends and pets through meaningful roles, readings and symbolic moments."
			),
			faq(
					"Can we have a wedding ceremony outdoors?",
					"Yes, celebrant-led ceremonies can be held outdoors or in almost any venue, subject to permissions, safety and a weather backup plan."
			),
			faq(
					"Can you create a completely bespoke ceremony?",
					"Yes, each ceremony is written from scratch to reflect your relationship, values, personality and the atmosphere you want on the day."
			),
			faq(
					"Can we include cultural, spiritual or religious traditions?",
					"Yes, ceremonies can include cultural, spiritual and religious traditions in a way that feels respectful, authentic and personal to you."
			),
			faq(
					"What symbolic rituals can we include?",
					"Popular options include handfasting, unity candles, sand ceremonies, ring warming, tree planting, wine rituals and family traditions."
			),
			faq(
					"Can we have a non-religious ceremony?",
					"Yes, celebrant ceremonies can be fully non-religious and focused on your relationship, commitments and shared values."
			),
			faq(
					"What is included in your celebrant fee?",
					"Every core package includes planning support, a bespoke ceremony and delivery on the day. Inclusions vary by package and can also cover revisions, rehearsals, venue visits, vow support, symbolic rituals, certificates and keepsakes."
			),
			faq(
					"Do you travel?",
					"Yes. Core ceremony packages include travel within 30 miles, and additional travel is charged at 50p per mile. The Signature wedding venue visit is available within 40 miles. Any travel beyond these limits is agreed in advance."
			),
			faq(
					"What happens if it rains during an outdoor ceremony?",
					"A practical wet-weather backup plan is agreed in advance, such as a covered area or an indoor alternative at the venue."
			),
			faq(
					"Do you offer virtual consultations?",
					"Yes, virtual consultations are available by video call, making planning simple and flexible regardless of location."
			),
			faq(
					"What is the difference between a celebrant and a registrar?",
					"A registrar conducts or attends the civil ceremony that fulfils the legal marriage requirements. An independent celebrant such as Clare creates and leads a personalised ceremony but does not register the marriage. Many couples choose both."
			),
			faq(
					"Where can we hold a celebrant-led wedding ceremony?",
					"Clare can lead a non-legally binding ceremony in many settings, including gardens, beaches, woodland, family homes and wedding venues, subject to the owner's permission, safety and practical arrangements. The separate legal marriage must follow the requirements for your chosen legal route."
			),
			faq(
					"Are celebrant weddings worth it?",
					"Many couples feel celebrant weddings are worth it because they offer a deeply personal and memorable ceremony experience."
			),
			faq(
					"How do I choose the right wedding celebrant?",
					"Choose someone whose tone, personality and approach match your vision, then confirm fit through an introductory conversation and reviews."
			),
			faq(
					"What questions should I ask a celebrant before booking?",
					"Ask about availability, pricing, planning process, ceremony style, travel, inclusions and how support works from enquiry to ceremony day."
			),
			faq(
					"Do celebrants attend wedding rehearsals?",
					"Many celebrants offer rehearsal guidance or attendance to help everyone feel clear, calm and prepared before the ceremony."
			),
			faq(
					"Can a celebrant perform a same-sex wedding ceremony?",
					"Yes, celebrants regularly create and lead same-sex wedding ceremonies that are inclusive, personal and respectful."
			),
			faq(
					"How much does a wedding celebrant cost in the UK?",
					"Clare's Wedding Ceremony packages are £725 for Essential, £925 for Signature and £1,195 for the Complete Ceremony Experience. Optional extras and travel beyond the included mileage are priced separately."
			),
			faq(
					"Is a celebrant wedding legally binding?",
					"As the law currently stands in England and Wales, Clare's independent celebrant-led ceremony is not legally binding by itself. Couples choosing the civil route complete the legal marriage separately with a registrar."
			),
			faq(
					"Can a celebrant perform an outdoor wedding?",
					"Yes, celebrants commonly lead outdoor wedding ceremonies in locations such as gardens, beaches, woodlands and private venues, with practical weather contingencies planned in advance."
			),
			faq(
					"Can a celebrant include religious elements?",
					"Yes, religious elements can be included when meaningful, alongside spiritual, cultural and personal traditions, to create a ceremony that reflects your beliefs and values."
			),
			faq(
					"How far in advance should I book a wedding celebrant?",
					"Booking 12 to 18 months in advance is recommended to secure your preferred date and allow time for a fully personalised ceremony planning process."
			)
	);

	private final BlogService blogService;

	private final AnalyticsService analyticsService;

	private final InquiryNotificationService inquiryNotificationService;

	private final ReviewService reviewService;

	private final ReviewProperties reviewProperties;

	private final ObjectMapper objectMapper = new ObjectMapper();

	public SiteController(BlogService blogService,
			AnalyticsService analyticsService,
			InquiryNotificationService inquiryNotificationService,
			ReviewService reviewService,
			ReviewProperties reviewProperties) {
		this.blogService = blogService;
		this.analyticsService = analyticsService;
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
		model.addAttribute("pageDescription", "Wedding, Celebration of Life, naming ceremony and vow renewal services by Clare's Life Celebrations, created for moments that matter.");
		model.addAttribute("serviceFaqs", SERVICE_FAQS);
		model.addAttribute("structuredDataJson", buildFaqStructuredDataJson(SERVICE_FAQS));
		return "ceremonies";
	}

	@GetMapping("/faq")
	public String faq(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Celebrant FAQ");
		model.addAttribute("pageDescription", "Answers to common celebrant questions, including legal wedding details, pricing, vows, ceremony planning and location flexibility.");
		model.addAttribute("faqEntries", FAQ_PAGE_ENTRIES);
		model.addAttribute("structuredDataJson", buildFaqStructuredDataJson(FAQ_PAGE_ENTRIES));
		return "faq";
	}

	@GetMapping("/weddings")
	public String weddings(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Wedding ceremonies in Durham");
		model.addAttribute("pageDescription", "Bespoke celebrant-led wedding ceremonies by Clare Brunton, designed with freedom, warmth, personality and meaningful ceremony choices.");
		return "weddings";
	}

	@GetMapping("/celebrations-of-life")
	public String celebrationsOfLife(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Celebration of Life ceremonies in Durham");
		model.addAttribute("pageDescription", "Celebration of Life and memorial ceremonies by Clare Brunton, created with compassion, dignity and heartfelt personal tribute.");
		return "funerals";
	}

	@GetMapping("/funerals")
	public ResponseEntity<Void> funeralsRedirect() {
		return permanentRedirect("/celebrations-of-life");
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
			ReviewEntry submittedReview = reviewService.submitReview(reviewForm, reviewPhotos);
			inquiryNotificationService.notifyReviewSubmitted(submittedReview);
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
			ReviewEntry submittedReview = reviewService.submitReview(reviewForm, reviewPhotos);
			inquiryNotificationService.notifyReviewSubmitted(submittedReview);
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
		if (!areReviewAdminCredentialsConfigured()) {
			redirectAttributes.addFlashAttribute("reviewAdminError", "Admin login is not configured. Set REVIEWS_ADMIN_USERNAME and REVIEWS_ADMIN_PASSWORD in the environment.");
			return "redirect:/reviews/admin/login";
		}

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
		model.addAttribute("analyticsSummary", analyticsService.getSummary());
		return "reviews-admin";
	}

	@GetMapping("/reviews/admin/manage")
	public String manageReviews(Model model, HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Manage all reviews");
		model.addAttribute("pageDescription", "Enable, disable, approve, reject or delete saved reviews.");
		model.addAttribute("robotsContent", "noindex, nofollow");
		model.addAttribute("managedReviews", reviewService.getManageableReviews());
		return "reviews-admin-manage";
	}

	@PostMapping("/reviews/admin/{reviewId}/approve")
	public String approveReview(@PathVariable String reviewId,
			@RequestParam(name = "note", required = false) String note,
			@RequestParam(name = "returnTo", required = false) String returnTo,
			RedirectAttributes redirectAttributes,
			HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		ReviewEntry approvedReview = reviewService.approveReview(reviewId, note);
		inquiryNotificationService.notifyReviewReady(approvedReview);
		redirectAttributes.addFlashAttribute("reviewAdminMessage", "Review approved.");
		return redirectAfterReviewAction(returnTo);
	}

	@PostMapping("/reviews/admin/{reviewId}/reject")
	public String rejectReview(@PathVariable String reviewId,
			@RequestParam(name = "note", required = false) String note,
			@RequestParam(name = "returnTo", required = false) String returnTo,
			RedirectAttributes redirectAttributes,
			HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		reviewService.rejectReview(reviewId, note);
		redirectAttributes.addFlashAttribute("reviewAdminMessage", "Review rejected and deleted.");
		return redirectAfterReviewAction(returnTo);
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

	@PostMapping("/reviews/admin/{reviewId}/delete")
	public String deleteReview(@PathVariable String reviewId,
			RedirectAttributes redirectAttributes,
			HttpSession session) {
		if (!isReviewAdminAuthenticated(session)) {
			return "redirect:/reviews/admin/login";
		}

		reviewService.deleteReview(reviewId);
		redirectAttributes.addFlashAttribute("reviewAdminMessage", "Review deleted permanently.");
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
	public ResponseEntity<Void> ceremoniesRedirect() {
		return permanentRedirect("/services");
	}

	@GetMapping("/blog")
	public String blog(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Journal");
		model.addAttribute("pageDescription", "Journal notes on celebrant-led weddings, celebrations of life and planning meaningful moments.");
		model.addAttribute("posts", blogService.findAll());
		return "blog";
	}

	@GetMapping("/blog/how-to-shape-a-funeral-tribute-with-warmth-and-clarity")
	public ResponseEntity<Void> legacyCelebrationOfLifeBlogRedirect() {
		return permanentRedirect("/blog/how-to-shape-a-celebration-of-life-tribute");
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
		model.addAttribute("pageTitle", "Weddings and Celebrations of Life in Durham");
		model.addAttribute("pageDescription", "Weddings and Celebrations of Life in Durham, personally written and led by Clare Brunton with warmth, dignity and thoughtful attention to every story.");
		model.addAttribute("featuredReviews", reviewService.getApprovedFiveStarReviews());
	}

	private ResponseEntity<Void> permanentRedirect(String path) {
		return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
				.location(URI.create(path))
				.build();
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

	private static Map<String, String> faq(String question, String answer) {
		Map<String, String> faq = new LinkedHashMap<>();
		faq.put("question", question);
		faq.put("answer", answer);
		return faq;
	}

	private String buildFaqStructuredDataJson(List<Map<String, String>> faqs) {
		Map<String, Object> faqPage = new LinkedHashMap<>();
		faqPage.put("@context", "https://schema.org");
		faqPage.put("@type", "FAQPage");

		List<Map<String, Object>> mainEntity = new ArrayList<>();
		for (Map<String, String> faq : faqs) {
			Map<String, Object> question = new LinkedHashMap<>();
			question.put("@type", "Question");
			question.put("name", faq.get("question"));

			Map<String, Object> answer = new LinkedHashMap<>();
			answer.put("@type", "Answer");
			answer.put("text", faq.get("answer"));

			question.put("acceptedAnswer", answer);
			mainEntity.add(question);
		}
		faqPage.put("mainEntity", mainEntity);

		try {
			return objectMapper.writeValueAsString(faqPage);
		}
		catch (JsonProcessingException exception) {
			return null;
		}
	}

	private boolean isReviewAdminAuthenticated(HttpSession session) {
		Object value = session.getAttribute(REVIEW_ADMIN_SESSION_KEY);
		return value instanceof Boolean authenticated && authenticated;
	}

	private boolean areReviewAdminCredentialsConfigured() {
		return StringUtils.hasText(reviewProperties.getAdminUsername())
				&& StringUtils.hasText(reviewProperties.getAdminPassword());
	}

	private String redirectAfterReviewAction(String returnTo) {
		if ("manage".equalsIgnoreCase(returnTo)) {
			return "redirect:/reviews/admin/manage";
		}
		return "redirect:/reviews/admin";
	}

}
