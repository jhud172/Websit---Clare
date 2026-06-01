package co.uk.clarebrunton.ceremonies;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Properties;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import co.uk.clarebrunton.ceremonies.config.SiteProperties;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.model.ReviewEntry;
import co.uk.clarebrunton.ceremonies.model.ReviewStatus;
import co.uk.clarebrunton.ceremonies.service.InquiryNotificationService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

class InquiryNotificationServiceTest {

	private SiteProperties buildProperties() {
		SiteProperties props = new SiteProperties();
		props.setName("Clare's Life Celebrations");
		props.setContactEmail("clarebruntoncelebrant@gmail.com");
		props.setFromEmail("no-reply@clarebrunton.co.uk");
		return props;
	}

	private InquiryForm buildValidForm() {
		InquiryForm form = new InquiryForm();
		form.setFullName("Jane Smith");
		form.setEmail("jane@example.com");
		form.setPhone("07123456789");
		form.setServiceType("Wedding ceremony");
		form.setEventDate(LocalDate.now().plusMonths(6));
		form.setVenue("The Mill Barns");
		form.setMessage("Looking for a warm and personal wedding ceremony with meaning.");
		form.setPrivacyAccepted(true);
		return form;
	}

	private InquiryNotificationService buildService(ObjectProvider<JavaMailSender> provider, Path fallbackDirectory) {
		InquiryNotificationService service = new InquiryNotificationService(provider, buildProperties());
		ReflectionTestUtils.setField(service, "fallbackDirectory", fallbackDirectory.toString());
		return service;
	}

	private MimeMessage newMimeMessage() {
		return new MimeMessage(Session.getInstance(new Properties()));
	}

	private ReviewEntry buildReview(String id, String reviewerName, ReviewStatus status) {
		ReviewEntry review = new ReviewEntry();
		review.setId(id);
		review.setReviewerName(reviewerName);
		review.setStatus(status);
		review.setMessage("This is a thoughtful and personal ceremony review.");
		review.setCeremonyType("Wedding ceremony");
		review.setRating(5);
		return review;
	}

	private long countFallbackRecords(Path fallbackDirectory) throws Exception {
		try (var files = Files.list(fallbackDirectory)) {
			return files.count();
		}
	}

	@Test
	void handleInquiryWritesFallbackWhenNoMailSenderConfigured(@TempDir Path fallbackDirectory) throws Exception {
		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(null);

		InquiryNotificationService service = buildService(provider, fallbackDirectory);

		assertThatCode(() -> service.handleInquiry(buildValidForm()))
				.doesNotThrowAnyException();
		assertThat(countFallbackRecords(fallbackDirectory)).isEqualTo(1);
	}

	@Test
	void handleInquiryWritesFallbackWhenAdminEmailFails(@TempDir Path fallbackDirectory) throws Exception {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		when(mailSender.createMimeMessage()).thenThrow(new MailSendException("SMTP unavailable"));

		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);

		InquiryNotificationService service = buildService(provider, fallbackDirectory);

		assertThatCode(() -> service.handleInquiry(buildValidForm()))
				.doesNotThrowAnyException();
		assertThat(countFallbackRecords(fallbackDirectory)).isEqualTo(1);
	}

	@Test
	void handleInquirySendsAdminAndConfirmationEmails() throws Exception {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		when(mailSender.createMimeMessage()).thenReturn(newMimeMessage(), newMimeMessage());

		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);

		InquiryNotificationService service = new InquiryNotificationService(provider, buildProperties());
		ReflectionTestUtils.setField(service, "notificationEmail", "clare-notifications@example.com");

		service.handleInquiry(buildValidForm(), List.of());

		ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender, times(2)).send(messageCaptor.capture());
		List<MimeMessage> sentMessages = messageCaptor.getAllValues();

		assertThat(sentMessages).hasSize(2);
		assertThat(sentMessages.get(0).getSubject()).isEqualTo("New enquiry from Jane Smith - Wedding ceremony");
		assertThat(sentMessages.get(0).getAllRecipients()[0].toString()).isEqualTo("clare-notifications@example.com");
		assertThat(sentMessages.get(1).getSubject()).isEqualTo("Your enquiry has been received - Clare's Life Celebrations");
		assertThat(sentMessages.get(1).getAllRecipients()[0].toString()).isEqualTo("jane@example.com");
	}

	@Test
	void handleInquiryDoesNotWriteFallbackWhenOnlyConfirmationEmailFails(@TempDir Path fallbackDirectory) throws Exception {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		when(mailSender.createMimeMessage()).thenReturn(newMimeMessage(), newMimeMessage());
		AtomicInteger sendCount = new AtomicInteger();
		org.mockito.Mockito.doAnswer(invocation -> {
			if (sendCount.incrementAndGet() == 2) {
				throw new MailSendException("SMTP error");
			}
			return null;
		}).when(mailSender).send(any(MimeMessage.class));

		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);

		InquiryNotificationService service = buildService(provider, fallbackDirectory);

		assertThatCode(() -> service.handleInquiry(buildValidForm(), List.of()))
				.doesNotThrowAnyException();
		assertThat(countFallbackRecords(fallbackDirectory)).isZero();
	}

	@Test
	void notifyReviewSubmittedSendsEmailToConfiguredRecipient() throws Exception {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		when(mailSender.createMimeMessage()).thenReturn(newMimeMessage());

		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);

		InquiryNotificationService service = new InquiryNotificationService(provider, buildProperties());
		ReflectionTestUtils.setField(service, "reviewNotificationEmail", "clare-reviews@example.com");

		service.notifyReviewSubmitted(buildReview("review-1", "Alex Smith", ReviewStatus.PENDING));

		ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender).send(messageCaptor.capture());
		MimeMessage sent = messageCaptor.getValue();
		assertThat(sent.getSubject()).isEqualTo("New review submitted by Alex Smith");
		assertThat(sent.getAllRecipients()[0].toString()).isEqualTo("clare-reviews@example.com");
	}

	@Test
	void notifyReviewReadySendsEmailToConfiguredRecipient() throws Exception {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		when(mailSender.createMimeMessage()).thenReturn(newMimeMessage());

		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);

		InquiryNotificationService service = new InquiryNotificationService(provider, buildProperties());
		ReflectionTestUtils.setField(service, "reviewNotificationEmail", "clare-reviews@example.com");

		service.notifyReviewReady(buildReview("review-2", "Sam Taylor", ReviewStatus.APPROVED));

		ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender).send(messageCaptor.capture());
		MimeMessage sent = messageCaptor.getValue();
		assertThat(sent.getSubject()).isEqualTo("Review approved from Sam Taylor");
		assertThat(sent.getAllRecipients()[0].toString()).isEqualTo("clare-reviews@example.com");
	}

}
