package co.uk.clarebrunton.ceremonies;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.web.multipart.MultipartFile;

import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.model.InquiryRecord;
import co.uk.clarebrunton.ceremonies.repository.InquiryRepository;
import co.uk.clarebrunton.ceremonies.service.InquiryNotificationService;
import co.uk.clarebrunton.ceremonies.service.InquiryService;
import co.uk.clarebrunton.ceremonies.service.MediaStorage;
import co.uk.clarebrunton.ceremonies.service.UploadInspector;

class InquiryServiceTest {

	private InquiryRepository inquiries;
	private MediaStorage mediaStorage;
	private UploadInspector uploadInspector;
	private InquiryNotificationService notifications;
	private InquiryService service;

	@BeforeEach
	void setUp() {
		inquiries = mock(InquiryRepository.class);
		mediaStorage = mock(MediaStorage.class);
		uploadInspector = mock(UploadInspector.class);
		notifications = mock(InquiryNotificationService.class);
		service = new InquiryService(inquiries, mediaStorage, uploadInspector, notifications);
		when(inquiries.save(any(InquiryRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void savesTheEnquiryBeforeAttemptingNotification() {
		InquiryForm form = validForm();

		service.submit(form, List.of());

		InOrder order = inOrder(inquiries, notifications);
		order.verify(inquiries).save(any(InquiryRecord.class));
		order.verify(notifications).handleInquiry(any(String.class), eq(form), eq(List.of()));
	}

	@Test
	void rejectsMoreThanThreeAttachmentsBeforeReadingOrSavingThem() {
		List<MultipartFile> attachments = List.of(file(), file(), file(), file());

		assertThatThrownBy(() -> service.submit(validForm(), attachments))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Please attach no more than three files.");

		verify(uploadInspector, never()).inspect(any(), any());
		verify(inquiries, never()).save(any());
	}

	private InquiryForm validForm() {
		InquiryForm form = new InquiryForm();
		form.setFullName("Browser Verification");
		form.setEmail("browser-verification@example.invalid");
		form.setServiceType("Wedding ceremony");
		form.setDatePreference("Flexible");
		form.setPrivacyAccepted(true);
		return form;
	}

	private MultipartFile file() {
		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		return file;
	}
}
