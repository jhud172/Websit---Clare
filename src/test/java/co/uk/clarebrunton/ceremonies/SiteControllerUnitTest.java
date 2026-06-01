package co.uk.clarebrunton.ceremonies;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import co.uk.clarebrunton.ceremonies.config.ReviewProperties;
import co.uk.clarebrunton.ceremonies.controller.SiteController;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.model.ReviewEntry;
import co.uk.clarebrunton.ceremonies.model.ReviewStatus;
import co.uk.clarebrunton.ceremonies.service.BlogService;
import co.uk.clarebrunton.ceremonies.service.InquiryNotificationService;
import co.uk.clarebrunton.ceremonies.service.ReviewService;

class SiteControllerUnitTest {

	private final InquiryNotificationService inquiryNotificationService = mock(InquiryNotificationService.class);
	private final ReviewService reviewService = mock(ReviewService.class);
	private final SiteController controller = new SiteController(new BlogService(), inquiryNotificationService, reviewService, new ReviewProperties());

	@Test
	void launchRoutesReturnExpectedViews() {
		Model model = new ExtendedModelMap();
		when(reviewService.getApprovedFiveStarReviews()).thenReturn(List.of());
		when(reviewService.getApprovedReviews()).thenReturn(List.of());

		assertThat(controller.home(model)).isEqualTo("home");
		assertThat(controller.about(new ExtendedModelMap())).isEqualTo("about");
		assertThat(controller.services(new ExtendedModelMap())).isEqualTo("ceremonies");
		assertThat(controller.weddings(new ExtendedModelMap())).isEqualTo("weddings");
		assertThat(controller.funerals(new ExtendedModelMap())).isEqualTo("funerals");
		assertThat(controller.reviews(new ExtendedModelMap())).isEqualTo("reviews");
		assertThat(controller.contact(new ExtendedModelMap())).isEqualTo("redirect:/");
		assertThat(controller.privacy(new ExtendedModelMap())).isEqualTo("privacy");
		assertThat(controller.thankYou(new ExtendedModelMap())).isEqualTo("thank-you");
		assertThat(controller.ceremoniesRedirect()).isEqualTo("redirect:/services");
		verify(reviewService).getApprovedFiveStarReviews();
		verify(reviewService).getApprovedReviews();
	}

	@Test
	void contactRedirectsToHomeBecauseEnquiryUsesModal() {
		Model model = new ExtendedModelMap();

		String view = controller.contact(model);

		assertThat(view).isEqualTo("redirect:/");
	}

	@Test
	void submitContactReturnsHomeWithOpenModalWhenBindingHasErrors() {
		InquiryForm inquiryForm = new InquiryForm();
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(inquiryForm, "inquiryForm");
		bindingResult.rejectValue("fullName", "required", "Please add your full name.");
		when(reviewService.getApprovedFiveStarReviews()).thenReturn(List.of());
		Model model = new ExtendedModelMap();

		String view = controller.submitContact(
				inquiryForm,
				bindingResult,
				model,
				new RedirectAttributesModelMap()
		);

		assertThat(view).isEqualTo("home");
		assertThat(model.getAttribute("openEnquiryModal")).isEqualTo(true);
		verifyNoInteractions(inquiryNotificationService);
		verify(reviewService).getApprovedFiveStarReviews();
	}

	@Test
	void submitContactRedirectsWhenBindingIsValid() {
		InquiryForm inquiryForm = new InquiryForm();
		inquiryForm.setFullName("James Hudson");
		inquiryForm.setEmail("james@example.com");
		inquiryForm.setPhone("07123456789");
		inquiryForm.setServiceType("Wedding ceremony");
		inquiryForm.setVenue("The Mill Barns");
		inquiryForm.setMessage("We are looking for a warm, modern wedding ceremony with a personal tone.");
		inquiryForm.setPrivacyAccepted(true);

		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(inquiryForm, "inquiryForm");

		String view = controller.submitContact(
				inquiryForm,
				bindingResult,
				new ExtendedModelMap(),
				new RedirectAttributesModelMap()
		);

		assertThat(view).isEqualTo("redirect:/thank-you");
		verify(inquiryNotificationService).handleInquiry(inquiryForm);
	}

	@Test
	void reviewAdminLoginRedirectsWhenAlreadyAuthenticated() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("reviewAdminAuthenticated", true);

		String view = controller.reviewAdminLogin(new ExtendedModelMap(), session);

		assertThat(view).isEqualTo("redirect:/reviews/admin");
	}

	@Test
	void submitReviewAdminLoginRedirectsWithErrorForInvalidCredentials() {
		MockHttpSession session = new MockHttpSession();
		RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

		String view = controller.submitReviewAdminLogin("wrong", "credentials", redirectAttributes, session);

		assertThat(view).isEqualTo("redirect:/reviews/admin/login");
		assertThat(redirectAttributes.getFlashAttributes().get("reviewAdminError"))
				.isEqualTo("Login details were not recognised.");
		assertThat(session.getAttribute("reviewAdminAuthenticated")).isNull();
	}

	@Test
	void submitReviewAdminLoginRedirectsToDashboardForValidCredentials() {
		ReviewProperties properties = new ReviewProperties();
		properties.setAdminUsername("clare-admin");
		properties.setAdminPassword("top-secret");
		SiteController localController = new SiteController(new BlogService(), inquiryNotificationService, reviewService, properties);

		MockHttpSession session = new MockHttpSession();

		String view = localController.submitReviewAdminLogin(
				"clare-admin",
				"top-secret",
				new RedirectAttributesModelMap(),
				session
		);

		assertThat(view).isEqualTo("redirect:/reviews/admin");
		assertThat(session.getAttribute("reviewAdminAuthenticated")).isEqualTo(true);
	}

	@Test
	void reviewAdminRedirectsToLoginWhenNotAuthenticated() {
		String view = controller.reviewAdmin(new ExtendedModelMap(), new MockHttpSession());

		assertThat(view).isEqualTo("redirect:/reviews/admin/login");
		verifyNoInteractions(reviewService);
	}

	@Test
	void reviewAdminLoadsReviewsWhenAuthenticated() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("reviewAdminAuthenticated", true);
		when(reviewService.getPendingReviews()).thenReturn(List.of());

		String view = controller.reviewAdmin(new ExtendedModelMap(), session);

		assertThat(view).isEqualTo("reviews-admin");
		verify(reviewService).getPendingReviews();
	}

	@Test
	void manageReviewsRedirectsToLoginWhenNotAuthenticated() {
		String view = controller.manageReviews(new ExtendedModelMap(), new MockHttpSession());

		assertThat(view).isEqualTo("redirect:/reviews/admin/login");
		verifyNoInteractions(reviewService);
	}

	@Test
	void manageReviewsLoadsManageableReviewsWhenAuthenticated() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("reviewAdminAuthenticated", true);
		when(reviewService.getManageableReviews()).thenReturn(List.of());

		String view = controller.manageReviews(new ExtendedModelMap(), session);

		assertThat(view).isEqualTo("reviews-admin-manage");
		verify(reviewService).getManageableReviews();
	}

	@Test
	void approveReviewRedirectsToLoginWhenNotAuthenticated() {
		String view = controller.approveReview("review-123", "Looks good", new RedirectAttributesModelMap(), new MockHttpSession());

		assertThat(view).isEqualTo("redirect:/reviews/admin/login");
		verifyNoInteractions(reviewService);
	}

	@Test
	void approveReviewCallsServiceWhenAuthenticated() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("reviewAdminAuthenticated", true);
		RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
		ReviewEntry approvedEntry = new ReviewEntry();
		approvedEntry.setId("review-123");
		approvedEntry.setReviewerName("Jane Smith");
		approvedEntry.setStatus(ReviewStatus.APPROVED);
		when(reviewService.approveReview("review-123", "Looks good")).thenReturn(approvedEntry);

		String view = controller.approveReview("review-123", "Looks good", redirectAttributes, session);

		assertThat(view).isEqualTo("redirect:/reviews/admin");
		assertThat(redirectAttributes.getFlashAttributes().get("reviewAdminMessage")).isEqualTo("Review approved.");
		verify(reviewService).approveReview("review-123", "Looks good");
		verify(inquiryNotificationService).notifyReviewReady(approvedEntry);
	}

	@Test
	void rejectReviewCallsServiceWhenAuthenticated() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("reviewAdminAuthenticated", true);
		RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

		String view = controller.rejectReview("review-123", "Not suitable", redirectAttributes, session);

		assertThat(view).isEqualTo("redirect:/reviews/admin");
		assertThat(redirectAttributes.getFlashAttributes().get("reviewAdminMessage")).isEqualTo("Review rejected and deleted.");
		verify(reviewService).rejectReview("review-123", "Not suitable");
	}

	@Test
	void enableReviewCallsServiceWhenAuthenticated() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("reviewAdminAuthenticated", true);
		RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

		String view = controller.enableReview("review-123", redirectAttributes, session);

		assertThat(view).isEqualTo("redirect:/reviews/admin/manage");
		assertThat(redirectAttributes.getFlashAttributes().get("reviewAdminMessage")).isEqualTo("Review enabled.");
		verify(reviewService).enableReview("review-123");
	}

	@Test
	void disableReviewCallsServiceWhenAuthenticated() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("reviewAdminAuthenticated", true);
		RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

		String view = controller.disableReview("review-123", redirectAttributes, session);

		assertThat(view).isEqualTo("redirect:/reviews/admin/manage");
		assertThat(redirectAttributes.getFlashAttributes().get("reviewAdminMessage")).isEqualTo("Review disabled.");
		verify(reviewService).disableReview("review-123");
	}

	@Test
	void submitReviewShowsUploadErrorWhenServiceRejectsPhotos() {
		var reviewForm = controller.reviewForm();
		reviewForm.setReviewerName("Jane Smith");
		reviewForm.setCeremonyType("Wedding ceremony");
		reviewForm.setRating(5);
		reviewForm.setMessage("This ceremony was so personal and thoughtful from beginning to end.");
		reviewForm.setConsentAccepted(true);

		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(reviewForm, "reviewForm");
		Model model = new ExtendedModelMap();
		when(reviewService.getApprovedReviews()).thenReturn(List.of());
		var photo = new MockMultipartFile("reviewPhotos", "photo.jpg", "image/jpeg", new byte[] { 1, 2, 3 });
		org.mockito.Mockito.doThrow(new IllegalArgumentException("Please upload JPG, PNG or WEBP images only."))
				.when(reviewService).submitReview(reviewForm, List.of(photo));

		String view = controller.submitReview(
				reviewForm,
				bindingResult,
				List.of(photo),
				model,
				new RedirectAttributesModelMap()
		);

		assertThat(view).isEqualTo("reviews");
		assertThat(model.getAttribute("reviewUploadError")).isEqualTo("Please upload JPG, PNG or WEBP images only.");
		verify(reviewService).submitReview(reviewForm, List.of(photo));
		verify(reviewService).getApprovedReviews();
	}

	@Test
	void submitReviewRedirectsWhenValid() {
		var reviewForm = controller.reviewForm();
		reviewForm.setReviewerName("Jane Smith");
		reviewForm.setCeremonyType("Wedding ceremony");
		reviewForm.setRating(5);
		reviewForm.setMessage("This ceremony was so personal and thoughtful from beginning to end.");
		reviewForm.setConsentAccepted(true);
		ReviewEntry submittedEntry = new ReviewEntry();
		submittedEntry.setId("review-456");
		submittedEntry.setReviewerName("Jane Smith");
		submittedEntry.setStatus(ReviewStatus.PENDING);
		when(reviewService.submitReview(reviewForm, List.of())).thenReturn(submittedEntry);

		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(reviewForm, "reviewForm");
		RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

		String view = controller.submitReview(
				reviewForm,
				bindingResult,
				List.of(),
				new ExtendedModelMap(),
				redirectAttributes
		);

		assertThat(view).isEqualTo("redirect:/reviews");
		assertThat(redirectAttributes.getFlashAttributes().get("reviewSubmissionSuccess"))
				.isEqualTo("Thank you. Your review has been received and is now pending approval.");
		verify(reviewService).submitReview(reviewForm, List.of());
		verify(inquiryNotificationService).notifyReviewSubmitted(submittedEntry);
		verifyNoMoreInteractions(reviewService);
	}
}
