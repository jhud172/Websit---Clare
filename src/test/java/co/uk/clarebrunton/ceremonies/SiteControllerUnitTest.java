package co.uk.clarebrunton.ceremonies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import co.uk.clarebrunton.ceremonies.controller.SiteController;
import co.uk.clarebrunton.ceremonies.model.AnalyticsSummary;
import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.security.PublicSubmissionGuard;
import co.uk.clarebrunton.ceremonies.service.AnalyticsService;
import co.uk.clarebrunton.ceremonies.service.BlogService;
import co.uk.clarebrunton.ceremonies.service.InquiryNotificationService;
import co.uk.clarebrunton.ceremonies.service.InquiryService;
import co.uk.clarebrunton.ceremonies.service.ReviewService;

class SiteControllerUnitTest {

	private final AnalyticsService analytics = mock(AnalyticsService.class);
	private final InquiryNotificationService notifications = mock(InquiryNotificationService.class);
	private final InquiryService inquiries = mock(InquiryService.class);
	private final ReviewService reviews = mock(ReviewService.class);
	private final PublicSubmissionGuard guard = mock(PublicSubmissionGuard.class);
	private final SiteController controller = new SiteController(new BlogService(), analytics, notifications, inquiries, reviews, guard);

	@BeforeEach
	void defaults() {
		when(reviews.getApprovedFiveStarReviews()).thenReturn(List.of());
		when(reviews.getApprovedReviews()).thenReturn(List.of());
		when(reviews.getPendingReviews()).thenReturn(List.of());
		when(inquiries.findAll()).thenReturn(List.of());
		when(analytics.getSummary()).thenReturn(new AnalyticsSummary(0, 0, 0, 0, 0, 0, "No visits yet", 0, List.of()));
		when(analytics.getFunnelSummary()).thenReturn(Map.of());
	}

	@Test
	void publicRoutesIncludeDedicatedServices() {
		assertThat(controller.home(new ExtendedModelMap())).isEqualTo("home");
		assertThat(controller.weddings(new ExtendedModelMap())).isEqualTo("weddings");
		assertThat(controller.celebrationsOfLife(new ExtendedModelMap())).isEqualTo("funerals");
		assertThat(controller.namingCeremonies(new ExtendedModelMap())).isEqualTo("naming-ceremonies");
		assertThat(controller.vowRenewals(new ExtendedModelMap())).isEqualTo("vow-renewals");
	}

	@Test
	void legacyRoutesRemainPermanentRedirects() {
		assertThat(controller.funeralsRedirect().getHeaders().getLocation()).hasToString("/celebrations-of-life");
		assertThat(controller.ceremoniesRedirect().getHeaders().getLocation()).hasToString("/services");
	}

	@Test
	void invalidEnquiryReopensModalWithoutPersistence() {
		InquiryForm form = new InquiryForm();
		var errors = new BeanPropertyBindingResult(form, "inquiryForm");
		errors.rejectValue("fullName", "required", "Name required");
		var model = new ExtendedModelMap();
		String view = controller.submitContact(form, errors, List.of(), model, new RedirectAttributesModelMap(), request());
		assertThat(view).isEqualTo("home");
		assertThat(model.get("openEnquiryModal")).isEqualTo(true);
		verifyNoInteractions(inquiries);
	}

	@Test
	void validEnquiryPersistsBeforeRedirect() {
		InquiryForm form = validInquiry();
		var redirects = new RedirectAttributesModelMap();
		String view = controller.submitContact(form, new BeanPropertyBindingResult(form, "inquiryForm"), List.of(),
				new ExtendedModelMap(), redirects, request());
		assertThat(view).isEqualTo("redirect:/thank-you");
		verify(guard).checkInquiry(any(), any(), any(Long.class), any());
		verify(inquiries).submit(form, List.of());
	}

	@Test
	void dashboardLoadsAllWorkingAreas() {
		var model = new ExtendedModelMap();
		assertThat(controller.reviewAdmin(model)).isEqualTo("reviews-admin");
		assertThat(model).containsKeys("pendingReviews", "analyticsSummary", "funnelSummary", "inquiries", "inquiryStatuses");
	}

	private MockHttpServletRequest request() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("127.0.0.1");
		return request;
	}

	private InquiryForm validInquiry() {
		InquiryForm form = new InquiryForm();
		form.setFullName("James Hudson");
		form.setEmail("james@example.com");
		form.setServiceType("Wedding ceremony");
		form.setDatePreference("Not decided yet");
		form.setPrivacyAccepted(true);
		form.setFormStartedAt(System.currentTimeMillis() - 2_000);
		return form;
	}
}
