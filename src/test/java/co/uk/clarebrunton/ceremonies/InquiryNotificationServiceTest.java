package co.uk.clarebrunton.ceremonies;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import co.uk.clarebrunton.ceremonies.config.SiteProperties;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.service.InquiryNotificationService;

class InquiryNotificationServiceTest {

	private SiteProperties buildProperties() {
		SiteProperties props = new SiteProperties();
		props.setName("Clare’s Life Celebrations");
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

	@Test
	void handleInquiryLogsAndDoesNotThrowWhenNoMailSenderConfigured() {
		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(null);

		InquiryNotificationService service = new InquiryNotificationService(provider, buildProperties());

		assertThatCode(() -> service.handleInquiry(buildValidForm()))
				.doesNotThrowAnyException();
	}

	@Test
	void handleInquiryDoesNotThrowWhenAdminEmailFails() {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		when(mailSender.createMimeMessage()).thenThrow(new MailSendException("SMTP unavailable"));

		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);

		InquiryNotificationService service = new InquiryNotificationService(provider, buildProperties());

		assertThatCode(() -> service.handleInquiry(buildValidForm()))
				.doesNotThrowAnyException();
	}

	@Test
	void handleInquiryDoesNotThrowWhenConfirmationEmailFails() throws Exception {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		jakarta.mail.internet.MimeMessage mimeMessage = mock(jakarta.mail.internet.MimeMessage.class);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		// Confirmation send throws
		org.mockito.Mockito.doThrow(new MailSendException("SMTP error"))
				.when(mailSender).send(any(SimpleMailMessage.class));

		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);

		InquiryNotificationService service = new InquiryNotificationService(provider, buildProperties());

		assertThatCode(() -> service.handleInquiry(buildValidForm(), List.of()))
				.doesNotThrowAnyException();
	}

}
