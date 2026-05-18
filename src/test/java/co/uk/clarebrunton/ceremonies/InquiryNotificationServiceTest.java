package co.uk.clarebrunton.ceremonies;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import co.uk.clarebrunton.ceremonies.config.SiteProperties;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.service.InquiryNotificationService;

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
	void handleInquiryDoesNotWriteFallbackWhenOnlyConfirmationEmailFails(@TempDir Path fallbackDirectory) throws Exception {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		jakarta.mail.internet.MimeMessage mimeMessage = mock(jakarta.mail.internet.MimeMessage.class);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		// Confirmation send throws
		org.mockito.Mockito.doThrow(new MailSendException("SMTP error"))
				.when(mailSender).send(any(SimpleMailMessage.class));

		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);

		InquiryNotificationService service = buildService(provider, fallbackDirectory);

		assertThatCode(() -> service.handleInquiry(buildValidForm(), List.of()))
				.doesNotThrowAnyException();
		assertThat(countFallbackRecords(fallbackDirectory)).isZero();
	}

}
