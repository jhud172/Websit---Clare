package co.uk.clarebrunton.ceremonies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import co.uk.clarebrunton.ceremonies.model.ReviewForm;
import co.uk.clarebrunton.ceremonies.model.ReviewStatus;
import co.uk.clarebrunton.ceremonies.repository.ReviewRepository;
import co.uk.clarebrunton.ceremonies.repository.StoredAssetRepository;
import co.uk.clarebrunton.ceremonies.service.ReviewService;

@SpringBootTest(properties = { "reviews.max-photo-count=2", "reviews.max-photo-size-bytes=1048576" })
class ReviewServiceTest {

	@Autowired ReviewService service;
	@Autowired ReviewRepository reviews;
	@Autowired StoredAssetRepository assets;

	@BeforeEach
	void reset() {
		reviews.deleteAll();
		assets.deleteAll();
	}

	@Test
	void curatedClientReviewRemainsAvailableWithoutDemoData() {
		assertThat(service.getApprovedReviews()).singleElement().satisfies(review -> {
			assertThat(review.getId()).isEqualTo("client-review-jessica-wedding-2026-05-24");
			assertThat(review.getReviewerName()).isEqualTo("Jessica");
			assertThat(review.getStatus()).isEqualTo(ReviewStatus.APPROVED);
		});
	}

	@Test
	void pendingReviewPersistsAndCanBeModerated() {
		var submitted = service.submitReview(validForm(), List.of());
		assertThat(reviews.findById(submitted.getId())).isPresent();
		assertThat(service.getPendingReviews()).hasSize(1);

		service.approveReview(submitted.getId(), "Permission checked");
		assertThat(service.getPendingReviews()).isEmpty();
		assertThat(service.getApprovedReviews()).hasSize(2);

		service.disableReview(submitted.getId());
		assertThat(service.getApprovedReviews()).hasSize(1);
	}

	@Test
	void realImageSignatureIsStoredAndRenamed() {
		byte[] jpeg = new byte[] { (byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00, 0x01 };
		var photo = new MockMultipartFile("reviewPhotos", "family.jpg", "image/jpeg", jpeg);
		var submitted = service.submitReview(validForm(), List.of(photo));
		String key = submitted.getPhotoFileNames().get(0);
		assertThat(key).startsWith("reviews-").endsWith(".jpg");
		assertThat(service.loadPhoto(key)).isPresent();
		assertThat(assets.count()).isEqualTo(1);
	}

	@Test
	void renamedExecutableIsRejectedByMagicBytes() {
		var fake = new MockMultipartFile("reviewPhotos", "not-really.jpg", "image/jpeg", "MZ executable".getBytes());
		assertThatThrownBy(() -> service.submitReview(validForm(), List.of(fake)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("did not match");
	}

	@Test
	void repeatedSubmissionTokenDoesNotCreateDuplicateReview() {
		ReviewForm form = validForm();
		form.setSubmissionToken("f4bd946a-cd7a-46e9-8197-348cd9f4d385");
		var first = service.submitReview(form, List.of());
		var repeat = service.submitReview(form, List.of());
		assertThat(first.getId()).isEqualTo(repeat.getId());
		assertThat(first.isNewlySubmitted()).isTrue();
		assertThat(repeat.isNewlySubmitted()).isFalse();
		assertThat(reviews.count()).isEqualTo(1);
	}

	@Test
	void eventDateCanBeOmittedBecauseThePublicFormMarksItOptional() {
		ReviewForm form = validForm();
		form.setEventDate(null);

		var submitted = service.submitReview(form, List.of());

		assertThat(reviews.findById(submitted.getId()))
				.get()
				.extracting(review -> review.getEventDate())
				.isNull();
	}

	private ReviewForm validForm() {
		ReviewForm form = new ReviewForm();
		form.setReviewerName("Alex Smith");
		form.setReviewerRole("Wedding couple");
		form.setCeremonyType("Wedding ceremony");
		form.setRating(5);
		form.setHeadline("Beautiful ceremony");
		form.setMessage("Clare created a warm and memorable ceremony that captured us perfectly and felt completely personal.");
		form.setEventDate(LocalDate.now().minusDays(10));
		form.setConsentAccepted(true);
		return form;
	}
}
