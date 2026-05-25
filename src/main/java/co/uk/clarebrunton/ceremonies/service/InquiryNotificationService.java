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
		catch (MailException | MessagingException exception) {
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
		MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

		if (StringUtils.hasText(siteProperties.getFromEmail())) {
			helper.setFrom(siteProperties.getFromEmail());
		}
		helper.setTo(recipient);
		helper.setReplyTo(inquiryForm.getEmail());
		helper.setSubject("New enquiry from " + inquiryForm.getFullName() + " - " + inquiryForm.getServiceType());
		helper.setText(buildAdminPlainText(inquiryForm, attachments), buildAdminHtml(inquiryForm, attachments));

		for (MultipartFile attachment : attachments) {
			String originalName = attachment.getOriginalFilename();
			String filename = StringUtils.hasText(originalName)
					? Paths.get(originalName).getFileName().toString()
					: "attachment";
			helper.addAttachment(filename, attachment);
		}

		return message;
	}

	private MimeMessage createConfirmationMessage(InquiryForm inquiryForm) throws MessagingException {
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (mailSender == null) {
			throw new MessagingException("No mail sender available for confirmation email.");
		}

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
		if (StringUtils.hasText(siteProperties.getFromEmail())) {
			helper.setFrom(siteProperties.getFromEmail());
		}
		helper.setTo(inquiryForm.getEmail());
		helper.setReplyTo(siteProperties.getContactEmail());
		helper.setSubject("Your enquiry has been received - " + siteProperties.getName());
		helper.setText(buildConfirmationPlainText(inquiryForm), buildConfirmationHtml(inquiryForm));
		return message;
	}

	private String buildAdminPlainText(InquiryForm inquiryForm, List<MultipartFile> attachments) {
		return """
				New website enquiry
				%s

				Client
				Name: %s
				Email: %s
				Phone: %s

				Ceremony details
				Service: %s
				Event date: %s
				Venue: %s
				Attachments: %s

				Message
				%s

				Reply directly to this email to respond to %s.
				""".formatted(
				siteProperties.getName(),
				orNotSupplied(inquiryForm.getFullName()),
				orNotSupplied(inquiryForm.getEmail()),
				orNotSupplied(inquiryForm.getPhone()),
				orNotSupplied(inquiryForm.getServiceType()),
				inquiryForm.getEventDate() != null ? inquiryForm.getEventDate() : "Not supplied",
				orNotSupplied(inquiryForm.getVenue()),
				attachments.isEmpty() ? "None" : attachments.size() + " file(s) attached",
				orNotSupplied(inquiryForm.getMessage()),
				orNotSupplied(inquiryForm.getFullName())
		);
	}

	private String buildAdminHtml(InquiryForm inquiryForm, List<MultipartFile> attachments) {
		String attachmentSummary = attachments.isEmpty() ? "None" : attachments.size() + " file(s) attached";
		return emailShell(
				"New website enquiry",
				"New enquiry received",
				"Someone has submitted the enquiry form on " + escapeHtml(siteProperties.getName()) + ".",
				"""
						%s
						%s
						%s
						<div style="height:18px;line-height:18px;">&nbsp;</div>
						%s
						<div style="height:18px;line-height:18px;">&nbsp;</div>
						<div style="padding:18px;border-radius:16px;background:#f7f4ed;border:1px solid #e3d7bc;">
							<div style="margin:0 0 8px;color:#5f6f69;font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;">Client message</div>
							<div style="margin:0;color:#25272a;font-size:16px;line-height:1.65;white-space:pre-wrap;">%s</div>
						</div>
						<div style="height:18px;line-height:18px;">&nbsp;</div>
						<div style="padding:14px 16px;border-radius:14px;background:#101317;color:#f4f0e8;font-size:14px;line-height:1.55;">
							Reply directly to this email to respond to %s.
						</div>
						""".formatted(
						detailTable(
								detailRow("Name", inquiryForm.getFullName())
										+ detailRow("Email", inquiryForm.getEmail())
										+ detailRow("Phone", inquiryForm.getPhone())
						),
						sectionTitle("Ceremony details"),
						detailTable(
								detailRow("Service", inquiryForm.getServiceType())
										+ detailRow("Event date", inquiryForm.getEventDate() != null ? inquiryForm.getEventDate().toString() : "Not supplied")
										+ detailRow("Venue", inquiryForm.getVenue())
										+ detailRow("Attachments", attachmentSummary)
						),
						sectionTitle("Message"),
						escapeHtml(orNotSupplied(inquiryForm.getMessage())),
						escapeHtml(orNotSupplied(inquiryForm.getFullName()))
				)
		);
	}

	private String buildConfirmationPlainText(InquiryForm inquiryForm) {
		return """
				Hello %s,

				Thank you for getting in touch about your %s.

				This confirms your enquiry has been received safely. Clare will review your details and reply personally as soon as she can.

				Summary
				Service: %s
				Event date: %s
				Venue: %s

				If your plans are time-sensitive, you can follow up at %s.

				With thanks,
				%s
				""".formatted(
				resolveGreetingName(inquiryForm),
				orNotSupplied(inquiryForm.getServiceType()).toLowerCase(),
				orNotSupplied(inquiryForm.getServiceType()),
				inquiryForm.getEventDate() != null ? inquiryForm.getEventDate() : "Not supplied",
				orNotSupplied(inquiryForm.getVenue()),
				siteProperties.getContactEmail(),
				siteProperties.getName()
		);
	}

	private String buildConfirmationHtml(InquiryForm inquiryForm) {
		return emailShell(
				"Enquiry received",
				"Thank you, " + escapeHtml(resolveGreetingName(inquiryForm)),
				"Your enquiry has been received safely. Clare will review your details and reply personally.",
				"""
						%s
						%s
						<div style="height:18px;line-height:18px;">&nbsp;</div>
						<div style="padding:16px;border-radius:16px;background:#f7f4ed;border:1px solid #e3d7bc;color:#25272a;font-size:15px;line-height:1.6;">
							If your plans are time-sensitive, you can follow up at
							<a href="mailto:%s" style="color:#546b63;font-weight:700;text-decoration:none;">%s</a>.
						</div>
						""".formatted(
						sectionTitle("Your enquiry summary"),
						detailTable(
								detailRow("Service", inquiryForm.getServiceType())
										+ detailRow("Event date", inquiryForm.getEventDate() != null ? inquiryForm.getEventDate().toString() : "Not supplied")
										+ detailRow("Venue", inquiryForm.getVenue())
						),
						escapeHtml(siteProperties.getContactEmail()),
						escapeHtml(siteProperties.getContactEmail())
				)
		);
	}

	private String emailShell(String preheader, String heading, String intro, String bodyHtml) {
		return """
				<!doctype html>
				<html lang="en">
				<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>%s</title>
				</head>
				<body style="margin:0;padding:0;background:#121417;color:#25272a;font-family:Arial,Helvetica,sans-serif;">
					<div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;">%s</div>
					<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#121417;">
						<tr>
							<td align="center" style="padding:32px 16px;">
								<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:680px;border-collapse:collapse;background:#fffaf2;border:1px solid #2e3338;border-radius:24px;overflow:hidden;">
									<tr>
										<td style="padding:28px 30px;background:#171a1f;border-bottom:1px solid #343a41;">
											<div style="color:#d6c791;font-family:Georgia,'Times New Roman',serif;font-size:28px;line-height:1.1;">CLC</div>
											<div style="margin-top:6px;color:#f6f1e7;font-size:15px;font-weight:700;">%s</div>
											<div style="margin-top:3px;color:#aeb7bd;font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;">For Moments That Matter</div>
										</td>
									</tr>
									<tr>
										<td style="padding:30px;">
											<h1 style="margin:0;color:#25272a;font-family:Georgia,'Times New Roman',serif;font-size:34px;line-height:1.1;font-weight:400;">%s</h1>
											<p style="margin:12px 0 24px;color:#5d6268;font-size:16px;line-height:1.65;">%s</p>
											%s
										</td>
									</tr>
									<tr>
										<td style="padding:20px 30px;background:#f2eee6;border-top:1px solid #e1d8c8;color:#6a6f73;font-size:13px;line-height:1.5;">
											%s<br>
											<a href="mailto:%s" style="color:#546b63;text-decoration:none;font-weight:700;">%s</a>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</body>
				</html>
				""".formatted(
				escapeHtml(preheader),
				escapeHtml(preheader),
				escapeHtml(siteProperties.getName()),
				heading,
				escapeHtml(intro),
				bodyHtml,
				escapeHtml(siteProperties.getName()),
				escapeHtml(siteProperties.getContactEmail()),
				escapeHtml(siteProperties.getContactEmail())
		);
	}

	private String sectionTitle(String label) {
		return """
				<div style="margin:0 0 10px;color:#5f6f69;font-size:12px;font-weight:800;letter-spacing:0.16em;text-transform:uppercase;">%s</div>
				""".formatted(escapeHtml(label));
	}

	private String detailTable(String rows) {
		return """
				<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:separate;border-spacing:0;border:1px solid #e3d7bc;border-radius:16px;overflow:hidden;background:#fffdf8;">
					%s
				</table>
				""".formatted(rows);
	}

	private String detailRow(String label, String value) {
		return """
				<tr>
					<td style="width:34%%;padding:12px 14px;border-bottom:1px solid #eee4d2;color:#70757a;font-size:13px;font-weight:700;">%s</td>
					<td style="padding:12px 14px;border-bottom:1px solid #eee4d2;color:#25272a;font-size:15px;line-height:1.45;">%s</td>
				</tr>
				""".formatted(escapeHtml(label), escapeHtml(orNotSupplied(value)));
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
				CLARE'S LIFE CELEBRATIONS
				Inquiry fallback record
				============================================================

				Status
				Reason:      %s
				Received:    %s
				Site:        %s

				Client
				Name:        %s
				Email:       %s
				Phone:       %s

				Ceremony
				Service:     %s
				Event date:  %s
				Venue:       %s
				Attachments:
				%s

				Message
				------------------------------------------------------------
				%s
				""".formatted(
				reason,
				receivedAt,
				siteProperties.getName(),
				orNotSupplied(inquiryForm.getFullName()),
				orNotSupplied(inquiryForm.getEmail()),
				orNotSupplied(inquiryForm.getPhone()),
				orNotSupplied(inquiryForm.getServiceType()),
				inquiryForm.getEventDate() != null ? inquiryForm.getEventDate() : "Not supplied",
				orNotSupplied(inquiryForm.getVenue()),
				attachmentSummary,
				orNotSupplied(inquiryForm.getMessage())
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

	private String orNotSupplied(String value) {
		return StringUtils.hasText(value) ? value.trim() : "Not supplied";
	}

	private String escapeHtml(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

}
