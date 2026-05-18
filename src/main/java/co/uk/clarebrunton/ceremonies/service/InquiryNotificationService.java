package co.uk.clarebrunton.ceremonies.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import co.uk.clarebrunton.ceremonies.config.SiteProperties;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class InquiryNotificationService {

	private static final Logger logger = LoggerFactory.getLogger(InquiryNotificationService.class);

	private final ObjectProvider<JavaMailSender> mailSenderProvider;

	private final SiteProperties siteProperties;

	@Value("${inquiry.notification-email:}")
	private String notificationEmail;

	@Value("${inquiry.fallback-directory:data/inquiries}")
	private String fallbackDirectory;

	public InquiryNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider, SiteProperties siteProperties) {
		this.mailSenderProvider = mailSenderProvider;
		this.siteProperties = siteProperties;
	}

	public void handleInquiry(InquiryForm inquiryForm, List<MultipartFile> attachments) {
		String recipient = StringUtils.hasText(notificationEmail) ? notificationEmail : siteProperties.getContactEmail();
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		List<MultipartFile> safeAttachments = attachments == null ? List.of() : attachments;

		if (mailSender == null || !StringUtils.hasText(recipient)) {
			logger.info("Inquiry received (no outbound email configured): name={}, service={}, email={}, attachments={}",
					inquiryForm.getFullName(),
					inquiryForm.getServiceType(),
					inquiryForm.getEmail(),
					safeAttachments.size());
			writeFallbackRecord(inquiryForm, safeAttachments, "outbound-email-not-configured");
			return;
		}

		try {
			mailSender.send(createAdminMessage(mailSender, recipient, inquiryForm, safeAttachments));
			logger.info("Admin notification sent to {} for inquiry from {}", recipient, inquiryForm.getEmail());
		}
		catch (MailException | MessagingException exception) {
			logger.error("Admin notification could not be sent for inquiry from {}. Check SMTP settings.",
					inquiryForm.getEmail(), exception);
			writeFallbackRecord(inquiryForm, safeAttachments, "admin-email-send-failed");
		}

		try {
			mailSender.send(createConfirmationMessage(inquiryForm));
			logger.info("Confirmation email sent to {}", inquiryForm.getEmail());
		}
		catch (MailException exception) {
			logger.error("Confirmation email could not be sent to {}. Check SMTP settings.",
					inquiryForm.getEmail(), exception);
		}
	}

	public void handleInquiry(InquiryForm inquiryForm) {
		handleInquiry(inquiryForm, List.of());
	}

	private MimeMessage createAdminMessage(JavaMailSender mailSender,
			String recipient,
			InquiryForm inquiryForm,
			List<MultipartFile> attachments) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, !attachments.isEmpty(), StandardCharsets.UTF_8.name());

		if (StringUtils.hasText(siteProperties.getFromEmail())) {
			helper.setFrom(siteProperties.getFromEmail());
		}
		helper.setTo(recipient);
		helper.setReplyTo(inquiryForm.getEmail());
		helper.setSubject("New enquiry: " + inquiryForm.getServiceType() + " — " + inquiryForm.getFullName());
		helper.setText("""
				New enquiry received via %s

				────────────────────────────────
				Name:         %s
				Email:        %s
				Phone:        %s
				Service:      %s
				Event date:   %s
				Venue:        %s
				Attachments:  %s
				────────────────────────────────

				Message:
				%s

				────────────────────────────────
				Reply directly to this email to respond to %s.
				""".formatted(
				siteProperties.getName(),
				inquiryForm.getFullName(),
				inquiryForm.getEmail(),
				inquiryForm.getPhone(),
				inquiryForm.getServiceType(),
				inquiryForm.getEventDate() != null ? inquiryForm.getEventDate() : "Not supplied",
				inquiryForm.getVenue(),
				attachments.isEmpty() ? "None" : attachments.size() + " file(s) attached",
				inquiryForm.getMessage(),
				inquiryForm.getFullName()
		));

		for (MultipartFile attachment : attachments) {
			String originalName = attachment.getOriginalFilename();
			String filename = StringUtils.hasText(originalName)
					? Paths.get(originalName).getFileName().toString()
					: "attachment";
			helper.addAttachment(filename, attachment);
		}

		return message;
	}

	private SimpleMailMessage createConfirmationMessage(InquiryForm inquiryForm) {
		SimpleMailMessage message = new SimpleMailMessage();
		if (StringUtils.hasText(siteProperties.getFromEmail())) {
			message.setFrom(siteProperties.getFromEmail());
		}
		message.setTo(inquiryForm.getEmail());
		message.setSubject("Thank you for your enquiry to " + siteProperties.getName());
		message.setText("""
				Hello %s,

				Thank you for getting in touch about your %s.

				This is a quick confirmation to let you know your enquiry has been received safely.

				Clare will review your details and reply personally as soon as she can. If your plans are time-sensitive, you are also welcome to follow up on %s.

				With thanks,
				%s
				""".formatted(
				resolveGreetingName(inquiryForm),
				inquiryForm.getServiceType().toLowerCase(),
				siteProperties.getContactEmail(),
				siteProperties.getName()
		));
		return message;
	}

	private String resolveGreetingName(InquiryForm inquiryForm) {
		String fullName = inquiryForm.getFullName();
		if (!StringUtils.hasText(fullName)) {
			return "there";
		}

		String[] parts = fullName.trim().split("\\s+");
		return parts.length > 0 ? parts[0] : fullName.trim();
	}

	private void writeFallbackRecord(InquiryForm inquiryForm, List<MultipartFile> attachments, String reason) {
		if (!StringUtils.hasText(fallbackDirectory)) {
			logger.warn("Inquiry fallback record skipped because inquiry.fallback-directory is empty.");
			return;
		}

		try {
			Path directory = Paths.get(fallbackDirectory).toAbsolutePath().normalize();
			Files.createDirectories(directory);

			OffsetDateTime receivedAt = OffsetDateTime.now();
			String timestamp = receivedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).replaceAll("[^0-9A-Za-z-]", "-");
			String filename = "%s-%s.txt".formatted(timestamp, sanitiseFilenameSegment(inquiryForm.getFullName()));
			Path recordPath = directory.resolve(filename);

			Files.writeString(recordPath, buildFallbackRecord(inquiryForm, attachments, reason, receivedAt),
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE_NEW,
					StandardOpenOption.WRITE);
			logger.warn("Inquiry fallback record saved to {}", recordPath);
		}
		catch (Exception exception) {
			logger.error("Inquiry fallback record could not be saved. Manual follow-up may be required for {}.",
					inquiryForm.getEmail(), exception);
		}
	}

	private String buildFallbackRecord(InquiryForm inquiryForm,
			List<MultipartFile> attachments,
			String reason,
			OffsetDateTime receivedAt) {
		String attachmentSummary = attachments.isEmpty()
				? "None"
				: attachments.stream()
						.map(this::formatAttachmentSummary)
						.reduce((left, right) -> left + System.lineSeparator() + right)
						.orElse("None");

		return """
				Inquiry fallback record
				Reason:       %s
				Received at:  %s
				Site:         %s

				Name:         %s
				Email:        %s
				Phone:        %s
				Service:      %s
				Event date:   %s
				Venue:        %s
				Attachments:  %s

				Message:
				%s
				""".formatted(
				reason,
				receivedAt,
				siteProperties.getName(),
				inquiryForm.getFullName(),
				inquiryForm.getEmail(),
				inquiryForm.getPhone(),
				inquiryForm.getServiceType(),
				inquiryForm.getEventDate() != null ? inquiryForm.getEventDate() : "Not supplied",
				inquiryForm.getVenue(),
				attachmentSummary,
				inquiryForm.getMessage()
		);
	}

	private String formatAttachmentSummary(MultipartFile attachment) {
		String originalName = StringUtils.hasText(attachment.getOriginalFilename())
				? Paths.get(attachment.getOriginalFilename()).getFileName().toString()
				: "attachment";
		return "- %s (%s bytes)".formatted(originalName, attachment.getSize());
	}

	private String sanitiseFilenameSegment(String value) {
		if (!StringUtils.hasText(value)) {
			return "unknown";
		}
		String sanitised = value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
		return StringUtils.hasText(sanitised) ? sanitised : "unknown";
	}

}
