package co.uk.clarebrunton.ceremonies.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.service.BlogService;
import co.uk.clarebrunton.ceremonies.service.InquiryNotificationService;
import jakarta.validation.Valid;

@Controller
public class SiteController {

	private static final String LOGO_CLARE = "/images/brand/logo-clare.png";

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

	private final BlogService blogService;

	private final InquiryNotificationService inquiryNotificationService;

	public SiteController(BlogService blogService, InquiryNotificationService inquiryNotificationService) {
		this.blogService = blogService;
		this.inquiryNotificationService = inquiryNotificationService;
	}

	@ModelAttribute("serviceOptions")
	public List<String> serviceOptions() {
		return SERVICE_OPTIONS;
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Warm, elegant ceremonies for moments that matter");
		model.addAttribute("pageDescription", "Clare's Life Celebrations offers warm, personal weddings, funerals and life ceremonies with a modern premium feel.");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Meet Clare");
		model.addAttribute("pageDescription", "Meet Clare and discover the calm, caring approach behind Clare's Life Celebrations.");
		return "about";
	}

	@GetMapping("/services")
	public String services(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Services");
		model.addAttribute("pageDescription", "An overview of Clare’s wedding, funeral and life ceremony services.");
		return "ceremonies";
	}

	@GetMapping("/weddings")
	public String weddings(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Wedding ceremonies");
		model.addAttribute("pageDescription", "Modern wedding ceremonies shaped with warmth, style and personal detail.");
		return "weddings";
	}

	@GetMapping("/funerals")
	public String funerals(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Funerals");
		model.addAttribute("pageDescription", "Thoughtful funeral and memorial ceremonies created with warmth, clarity and care.");
		return "funerals";
	}

	@GetMapping("/reviews")
	public String reviews(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Reviews");
		model.addAttribute("pageDescription", "A dedicated page for kind words, feedback and the experience Clare is building around her celebrant services.");
		return "reviews";
	}

	@GetMapping("/ceremonies")
	public String ceremoniesRedirect() {
		return "redirect:/services";
	}

	@GetMapping("/blog")
	public String blog(Model model) {
		model.addAttribute("logoPath", LOGO_CLARE);
		model.addAttribute("pageTitle", "Journal");
		model.addAttribute("pageDescription", "Placeholder journal posts for wedding, funeral and celebrant planning.");
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
		model.addAttribute("logoPath", LOGO_CLARE);
		if (!model.containsAttribute("inquiryForm")) {
			model.addAttribute("inquiryForm", new InquiryForm());
		}
		model.addAttribute("pageTitle", "Contact");
		model.addAttribute("pageDescription", "Get in touch with Clare's Life Celebrations to start planning a wedding, funeral or milestone ceremony.");
		return "contact";
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
			model.addAttribute("logoPath", LOGO_CLARE);
			model.addAttribute("pageTitle", "Contact");
			model.addAttribute("pageDescription", "Get in touch with Clare's Life Celebrations to start planning a wedding, funeral or milestone ceremony.");
			if (attachmentError != null) {
				model.addAttribute("attachmentError", attachmentError);
			}
			return "contact";
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

}
