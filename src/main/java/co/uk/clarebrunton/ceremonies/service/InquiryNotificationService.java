package co.uk.clarebrunton.ceremonies.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import co.uk.clarebrunton.ceremonies.config.SiteProperties;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.model.ReviewEntry;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class InquiryNotificationService {

	private static final Logger logger = LoggerFactory.getLogger(InquiryNotificationService.class);

	private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

	private static final String BRAND_BANNER_CONTENT_ID = "clareBrandBanner";

	private static final String BRAND_BANNER_RESOURCE = "static/images/brand/logo-banner-no-background.png";

	private final ObjectProvider<JavaMailSender> mailSenderProvider;

	private final ObjectProvider<ResendEmailClient> resendEmailClientProvider;

	private final SiteProperties siteProperties;

	@Value("${inquiry.notification-email:}")
	private String notificationEmail;

	@Value("${inquiry.fallback-directory:data/inquiries}")
	private String fallbackDirectory;

	@Value("${reviews.notification-email:}")
	private String reviewNotificationEmail;

	public InquiryNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider, SiteProperties siteProperties) {
		this(mailSenderProvider, null, siteProperties);
	}

	@Autowired
	public InquiryNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider,
			ObjectProvider<ResendEmailClient> resendEmailClientProvider,
			SiteProperties siteProperties) {
		this.mailSenderProvider = mailSenderProvider;
		this.resendEmailClientProvider = resendEmailClientProvider;
		this.siteProperties = siteProperties;
	}

	public void handleInquiry(InquiryForm inquiryForm, List<MultipartFile> attachments) {
		String recipient = StringUtils.hasText(notificationEmail) ? notificationEmail : siteProperties.getContactEmail();
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		ResendEmailClient resendEmailClient = resendEmailClientProvider != null ? resendEmailClientProvider.getIfAvailable() : null;
		List<MultipartFile> safeAttachments = attachments == null ? List.of() : attachments;

		if (!StringUtils.hasText(recipient)) {
			logger.info("Inquiry received (no outbound email configured): name={}, service={}, email={}, attachments={}",
					inquiryForm.getFullName(),
					inquiryForm.getServiceType(),
					inquiryForm.getEmail(),
					safeAttachments.size());
			writeFallbackRecord(inquiryForm, safeAttachments, "outbound-email-not-configured");
			return;
		}

		if (resendEmailClient != null && resendEmailClient.isConfigured()) {
			sendInquiryWithResend(resendEmailClient, recipient, inquiryForm, safeAttachments);
			return;
		}

		if (mailSender == null) {
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
			logSmtpFailure("Admin notification", inquiryForm.getEmail(), exception);
			writeFallbackRecord(inquiryForm, safeAttachments, "admin-email-send-failed");
		}

		try {
			mailSender.send(createConfirmationMessage(inquiryForm));
			logger.info("Confirmation email sent to {}", inquiryForm.getEmail());
		}
		catch (MailException | MessagingException exception) {
			logSmtpFailure("Confirmation email", inquiryForm.getEmail(), exception);
		}
	}

	public void handleInquiry(InquiryForm inquiryForm) {
		handleInquiry(inquiryForm, List.of());
	}

	public void notifyReviewSubmitted(ReviewEntry review) {
		sendReviewNotification(review,
				"New review submitted by " + orNotSupplied(review.getReviewerName()),
				"New review awaiting moderation",
				"A new client review has been submitted and is ready for Clare to review in the moderation area.",
				"Review submitted for moderation",
				"Open the review admin area to approve or reject this review.");
	}

	public void notifyReviewReady(ReviewEntry review) {
		sendReviewNotification(review,
				"Review approved from " + orNotSupplied(review.getReviewerName()),
				"Review is now ready",
				"A client review has been approved and is ready on the website reviews page.",
				"Review approved",
				"This review is now available to appear publicly on the website.");
	}

	private void sendReviewNotification(ReviewEntry review,
			String subject,
			String heading,
			String intro,
			String statusLabel,
			String nextStep) {
		String recipient = StringUtils.hasText(reviewNotificationEmail) ? reviewNotificationEmail : siteProperties.getContactEmail();
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		ResendEmailClient resendEmailClient = resendEmailClientProvider != null ? resendEmailClientProvider.getIfAvailable() : null;

		if (!StringUtils.hasText(recipient)) {
			logger.info("Review notification skipped (no outbound email configured): reviewer={}, status={}",
					review.getReviewerName(), review.getStatus());
			return;
		}

		if (resendEmailClient != null && resendEmailClient.isConfigured()) {
			try {
				resendEmailClient.send(
						recipient,
						siteProperties.getContactEmail(),
						subject,
						buildReviewPlainText(review, statusLabel, nextStep),
						buildReviewHtml(review, heading, intro, statusLabel, nextStep),
						List.of());
				logger.info("Review notification sent via Resend to {} for review {}", recipient, review.getId());
			}
			catch (ResendEmailClient.EmailDeliveryException | IllegalStateException exception) {
				logger.error("Review notification could not be sent for review {}. Check Resend settings.", review.getId(), exception);
			}
			return;
		}

		if (mailSender == null) {
			logger.info("Review notification skipped (no outbound email configured): reviewer={}, status={}",
					review.getReviewerName(), review.getStatus());
			return;
		}

		try {
			mailSender.send(createReviewMessage(mailSender, recipient, review, subject, heading, intro, statusLabel, nextStep));
			logger.info("Review notification sent to {} for review {}", recipient, review.getId());
		}
		catch (MailException | MessagingException exception) {
			logSmtpFailure("Review notification", recipient, exception);
		}
	}

	private void logSmtpFailure(String emailType, String target, Exception exception) {
		if (isAuthenticationFailure(exception)) {
			logger.warn(
					"{} could not be sent to {} because SMTP authentication failed. "
							+ "If using Gmail, enable 2-Step Verification and use an App Password for SPRING_MAIL_PASSWORD.",
					emailType,
					target
			);
			logger.debug("SMTP authentication failure details", exception);
			return;
		}

		logger.error("{} could not be sent to {}. Check SMTP settings.", emailType, target, exception);
	}

	private boolean isAuthenticationFailure(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof AuthenticationFailedException) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private void sendInquiryWithResend(ResendEmailClient resendEmailClient,
			String recipient,
			InquiryForm inquiryForm,
			List<MultipartFile> attachments) {
		try {
			resendEmailClient.send(
					recipient,
					inquiryForm.getEmail(),
					"New enquiry from " + inquiryForm.getFullName() + " - " + inquiryForm.getServiceType(),
					buildAdminPlainText(inquiryForm, attachments),
					buildAdminHtml(inquiryForm, attachments),
					attachments);
			logger.info("Admin notification sent via Resend to {} for inquiry from {}", recipient, inquiryForm.getEmail());
		}
		catch (ResendEmailClient.EmailDeliveryException | IllegalStateException exception) {
			logger.error("Admin notification could not be sent for inquiry from {}. Check Resend settings.",
					inquiryForm.getEmail(), exception);
			writeFallbackRecord(inquiryForm, attachments, "admin-email-send-failed");
		}

		try {
			resendEmailClient.send(
					inquiryForm.getEmail(),
					siteProperties.getContactEmail(),
					"Your enquiry has been received - " + siteProperties.getName(),
					buildConfirmationPlainText(inquiryForm),
					buildConfirmationHtml(inquiryForm),
					List.of());
			logger.info("Confirmation email sent via Resend to {}", inquiryForm.getEmail());
		}
		catch (ResendEmailClient.EmailDeliveryException | IllegalStateException exception) {
			logger.error("Confirmation email could not be sent to {}. Check Resend settings.",
					inquiryForm.getEmail(), exception);
		}
	}

	private MimeMessage createReviewMessage(JavaMailSender mailSender,
			String recipient,
			ReviewEntry review,
			String subject,
			String heading,
			String intro,
			String statusLabel,
			String nextStep) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

		if (StringUtils.hasText(siteProperties.getFromEmail())) {
			helper.setFrom(siteProperties.getFromEmail());
		}
		helper.setTo(recipient);
		helper.setReplyTo(siteProperties.getContactEmail());
		helper.setSubject(subject);
		helper.setText(buildReviewPlainText(review, statusLabel, nextStep), buildReviewHtml(review, heading, intro, statusLabel, nextStep));
		addInlineBrandImages(helper);
		return message;
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
		addInlineBrandImages(helper);

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
		addInlineBrandImages(helper);
		return message;
	}

	private String buildAdminPlainText(InquiryForm inquiryForm, List<MultipartFile> attachments) {
		return """
				%s
				%s
				------------------------------------------------------------

				New enquiry received from %s

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
				resolveTagline(),
				orNotSupplied(inquiryForm.getFullName()),
				orNotSupplied(inquiryForm.getFullName()),
				orNotSupplied(inquiryForm.getEmail()),
				orNotSupplied(inquiryForm.getPhone()),
				orNotSupplied(inquiryForm.getServiceType()),
				formatDate(inquiryForm.getEventDate()),
				orNotSupplied(inquiryForm.getVenue()),
				attachments.isEmpty() ? "None" : attachments.size() + " file(s) attached",
				orNotSupplied(inquiryForm.getMessage()),
				orNotSupplied(inquiryForm.getFullName())
		);
	}

	private String buildAdminHtml(InquiryForm inquiryForm, List<MultipartFile> attachments) {
		String attachmentSummary = attachments.isEmpty() ? "None" : attachments.size() + " file(s) attached";
		return emailShell(
				"New enquiry from " + orNotSupplied(inquiryForm.getFullName()),
				"New enquiry received",
				"A new enquiry has been submitted through " + siteProperties.getName() + ". Reply directly to this email to respond.",
				"""
						%s
						%s
						%s
						%s
						%s
						""".formatted(
						highlightCard("Client", orNotSupplied(inquiryForm.getFullName()),
								"""
										%s
										%s
										""".formatted(
										contactLink("mailto:" + orNotSupplied(inquiryForm.getEmail()), orNotSupplied(inquiryForm.getEmail())),
										escapeHtml(orNotSupplied(inquiryForm.getPhone()))
								)
						),
						emailSpacer(18),
						emailSection("Ceremony details",
								detailRow("Service", inquiryForm.getServiceType())
										+ detailRow("Event date", formatDate(inquiryForm.getEventDate()))
										+ detailRow("Venue", inquiryForm.getVenue())
										+ detailRow("Attachments", attachmentSummary)
						),
						emailSpacer(18),
						messageCard("Client message", orNotSupplied(inquiryForm.getMessage())),
						emailSpacer(18),
						darkNotice("Reply directly to this email to respond to " + orNotSupplied(inquiryForm.getFullName()) + ".")
				)
		);
	}

	private String buildReviewPlainText(ReviewEntry review, String statusLabel, String nextStep) {
		return """
				%s
				%s
				------------------------------------------------------------

				%s

				Reviewer
				Name: %s
				Role: %s

				Review details
				Ceremony: %s
				Rating: %s/5
				Event date: %s
				Photos: %s

				Headline
				%s

				Review
				%s

				%s
				""".formatted(
				siteProperties.getName(),
				resolveTagline(),
				statusLabel,
				orNotSupplied(review.getReviewerName()),
				orNotSupplied(review.getReviewerRole()),
				orNotSupplied(review.getCeremonyType()),
				review.getRating(),
				formatDate(review.getEventDate()),
				formatPhotoCount(review),
				orNotSupplied(review.getHeadline()),
				orNotSupplied(review.getMessage()),
				nextStep
		);
	}

	private String buildReviewHtml(ReviewEntry review, String heading, String intro, String statusLabel, String nextStep) {
		return emailShell(
				heading,
				heading,
				intro,
				"""
						%s
						%s
						%s
						%s
						%s
						""".formatted(
						highlightCard(statusLabel, orNotSupplied(review.getReviewerName()),
								"%s<br>%s star review".formatted(
										escapeHtml(orNotSupplied(review.getCeremonyType())),
										review.getRating()
								)
						),
						emailSpacer(18),
						emailSection("Review details",
								detailRow("Reviewer role", review.getReviewerRole())
										+ detailRow("Event date", formatDate(review.getEventDate()))
										+ detailRow("Photos", formatPhotoCount(review))
						),
						emailSpacer(18),
						messageCard(orNotSupplied(review.getHeadline()), orNotSupplied(review.getMessage())),
						emailSpacer(18),
						darkNotice(nextStep)
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
				formatDate(inquiryForm.getEventDate()),
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
						%s
						%s
						""".formatted(
						emailSection("Your enquiry summary",
								detailRow("Service", inquiryForm.getServiceType())
										+ detailRow("Event date", formatDate(inquiryForm.getEventDate()))
										+ detailRow("Venue", inquiryForm.getVenue())
						),
						emailSpacer(18),
						messageCard("What happens next",
								"Clare will read your enquiry personally and come back to you with the next steps."),
						emailSpacer(18),
						darkNotice("If your plans are time-sensitive, you can follow up at " + siteProperties.getContactEmail() + ".")
				)
		);
	}

	private String emailShell(String preheader, String heading, String intro, String bodyHtml) {
		String bannerUrl = inlineImageSource(BRAND_BANNER_CONTENT_ID, "/images/brand/logo-banner-no-background.png");
		String siteUrl = StringUtils.hasText(siteProperties.getBaseUrl()) ? siteProperties.getBaseUrl() : "";
		return """
				<!doctype html>
				<html lang="en">
				<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>%s</title>
				</head>
				<body style="margin:0;padding:0;background:#070a0d;color:#25272a;font-family:Arial,Helvetica,sans-serif;">
					<div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;">%s</div>
					<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#070a0d;">
						<tr>
							<td align="center" style="padding:34px 12px;">
								<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:700px;border-collapse:collapse;background:#111317;border:1px solid #365f5e;border-radius:26px;overflow:hidden;box-shadow:0 24px 70px rgba(0,0,0,0.45);">
									<tr>
										<td style="padding:0;background:#080b0f;">
											<div style="padding:24px 28px 22px;background:linear-gradient(135deg,#070a0d 0%%,#0c292c 52%%,#111317 100%%);border-bottom:1px solid #5d5f36;">
												<img src="%s" width="620" alt="%s" style="display:block;width:100%%;max-width:620px;height:auto;margin:0 auto;border:0;">
											</div>
											<div style="height:4px;line-height:4px;background:linear-gradient(90deg,#c99a33 0%%,#27956f 34%%,#0076a8 68%%,#c99a33 100%%);">&nbsp;</div>
										</td>
									</tr>
									<tr>
										<td style="padding:34px 28px 30px;background:#191a17;">
											<p style="margin:0 0 12px;color:#f0c866;font-size:12px;font-weight:800;letter-spacing:0.2em;text-transform:uppercase;">Website notification</p>
											<h1 style="margin:0;color:#fffdf7;font-family:Georgia,'Times New Roman',serif;font-size:38px;line-height:1.08;font-weight:400;">%s</h1>
											<p style="margin:16px 0 26px;color:#e0e5e4;font-size:17px;line-height:1.66;">%s</p>
											%s
										</td>
									</tr>
									<tr>
										<td style="padding:0;background:#080b0f;">
											<div style="height:4px;line-height:4px;background:linear-gradient(90deg,#c99a33 0%%,#27956f 34%%,#0076a8 68%%,#c99a33 100%%);">&nbsp;</div>
											<div style="padding:22px 28px 24px;background:linear-gradient(135deg,#070a0d 0%%,#0c292c 52%%,#111317 100%%);border-top:1px solid #5d5f36;">
												<img src="%s" width="620" alt="%s" style="display:block;width:100%%;max-width:620px;height:auto;margin:0 auto 16px;border:0;">
												<div style="margin:0;color:#bfc7cb;font-size:13px;line-height:1.55;text-align:center;">
													<a href="mailto:%s" style="color:#f6f1e7;text-decoration:none;font-weight:700;">%s</a>
													&nbsp;&bull;&nbsp;
													<a href="%s" style="color:#f6f1e7;text-decoration:none;font-weight:700;">Website</a>
												</div>
											</div>
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
				escapeHtml(bannerUrl),
				escapeHtml(siteProperties.getName()),
				heading,
				escapeHtml(intro),
				bodyHtml,
				escapeHtml(bannerUrl),
				escapeHtml(siteProperties.getName()),
				escapeHtml(siteProperties.getContactEmail()),
				escapeHtml(siteProperties.getContactEmail()),
				escapeHtml(siteUrl)
		);
	}

	private String emailSection(String label, String rows) {
		return """
				<div style="margin:0 0 10px;color:#f0c866;font-size:12px;font-weight:800;letter-spacing:0.17em;text-transform:uppercase;">%s</div>
				<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:separate;border-spacing:0;border:1px solid #4f5e47;border-radius:18px;overflow:hidden;background:#101b1d;">
					%s
				</table>
				""".formatted(escapeHtml(label), rows);
	}

	private String highlightCard(String eyebrow, String heading, String copy) {
		return """
				<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;border-radius:20px;overflow:hidden;background:#0b2328;border:1px solid #69a6a2;">
					<tr>
						<td style="padding:22px 24px;background:linear-gradient(135deg,#0b2328 0%%,#123d40 54%%,#101716 100%%);">
							<div style="margin:0 0 10px;color:#f0c866;font-size:12px;font-weight:800;letter-spacing:0.2em;text-transform:uppercase;">%s</div>
							<div style="margin:0;color:#fffdf7;font-family:Georgia,'Times New Roman',serif;font-size:30px;line-height:1.14;">%s</div>
							<div style="margin-top:12px;color:#e8eeee;font-size:16px;line-height:1.66;font-weight:600;">%s</div>
						</td>
					</tr>
				</table>
				""".formatted(escapeHtml(eyebrow), escapeHtml(heading), copy);
	}

	private String detailRow(String label, String value) {
		return """
				<tr>
					<td style="width:34%%;padding:14px 15px;border-bottom:1px solid #334445;color:#ccd5d4;font-size:12px;font-weight:800;letter-spacing:0.06em;text-transform:uppercase;">%s</td>
					<td style="padding:14px 15px;border-bottom:1px solid #334445;color:#fffdf7;font-size:16px;line-height:1.45;font-weight:700;">%s</td>
				</tr>
				""".formatted(escapeHtml(label), escapeHtml(orNotSupplied(value)));
	}

	private String messageCard(String label, String message) {
		return """
				<div style="padding:20px;border-radius:18px;background:#fffaf0;border:1px solid #e6d59f;">
					<div style="margin:0 0 8px;color:#6a521f;font-size:12px;font-weight:800;letter-spacing:0.16em;text-transform:uppercase;">%s</div>
					<div style="margin:0;color:#1d2226;font-size:16px;line-height:1.68;font-weight:500;">%s</div>
				</div>
				""".formatted(escapeHtml(label), withLineBreaks(message));
	}

	private String darkNotice(String message) {
		return """
				<div style="padding:16px 18px;border-radius:16px;background:#0b2328;color:#fffdf7;border:1px solid #69a6a2;font-size:15px;line-height:1.58;font-weight:600;">
					%s
				</div>
				""".formatted(escapeHtml(message));
	}

	private String contactLink(String href, String label) {
		return """
				<a href="%s" style="color:#f6f1e7;text-decoration:none;font-weight:800;">%s</a>
				""".formatted(escapeHtml(href), escapeHtml(label));
	}

	private String emailSpacer(int height) {
		return "<div style=\"height:%spx;line-height:%spx;\">&nbsp;</div>".formatted(height, height);
	}

	private void addInlineBrandImages(MimeMessageHelper helper) throws MessagingException {
		addInlineImageIfPresent(helper, BRAND_BANNER_CONTENT_ID, BRAND_BANNER_RESOURCE);
	}

	private void addInlineImageIfPresent(MimeMessageHelper helper, String contentId, String resourcePath) throws MessagingException {
		Resource resource = new ClassPathResource(resourcePath);
		if (resource.exists()) {
			helper.addInline(contentId, resource);
		}
		else {
			logger.warn("Email inline image not found on classpath: {}", resourcePath);
		}
	}

	private String inlineImageSource(String contentId, String fallbackPath) {
		return absoluteAssetUrl(fallbackPath);
	}

	private String absoluteAssetUrl(String path) {
		String baseUrl = StringUtils.hasText(siteProperties.getBaseUrl())
				? siteProperties.getBaseUrl().replaceAll("/+$", "")
				: "";
		if (!StringUtils.hasText(baseUrl)) {
			return path;
		}
		return baseUrl + path;
	}

	private String resolveTagline() {
		return StringUtils.hasText(siteProperties.getTagline())
				? siteProperties.getTagline()
				: "For Moments That Matter";
	}

	private String formatDate(LocalDate date) {
		return date != null ? date.format(DISPLAY_DATE_FORMAT) : "Not supplied";
	}

	private String formatPhotoCount(ReviewEntry review) {
		List<String> photoFileNames = review.getPhotoFileNames();
		if (photoFileNames == null || photoFileNames.isEmpty()) {
			return "None";
		}
		return photoFileNames.size() + " photo(s) uploaded";
	}

	private String withLineBreaks(String value) {
		return escapeHtml(orNotSupplied(value)).replace("\r\n", "\n").replace("\n", "<br>");
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
