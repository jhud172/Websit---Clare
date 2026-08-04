package co.uk.clarebrunton.ceremonies;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import co.uk.clarebrunton.ceremonies.config.ReviewProperties;
import co.uk.clarebrunton.ceremonies.model.ReviewEntry;
import co.uk.clarebrunton.ceremonies.model.ReviewForm;
import co.uk.clarebrunton.ceremonies.model.ReviewStatus;
import co.uk.clarebrunton.ceremonies.service.ReviewService;

class ReviewServiceTest {

	private static final String JESSICA_REVIEW_ID = "client-review-jessica-wedding-2026-05-24";

	private ReviewService createService(Path tempDir) {
		ReviewProperties properties = new ReviewProperties();
		properties.setStorageDirectory(tempDir.toString());
		properties.setPhotoDirectory("uploads");
		properties.setMaxPhotoCount(2);
		properties.setMaxPhotoSizeBytes(1024 * 1024);
		return new ReviewService(properties);
	}

	private ReviewForm validForm() {
		ReviewForm form = new ReviewForm();
		form.setReviewerName("Alex Smith");
		form.setReviewerRole("Bride");
		form.setCeremonyType("Wedding ceremony");
		form.setRating(5);
		form.setHeadline("Beautiful ceremony");
		form.setMessage("Clare created a warm and memorable ceremony that captured us perfectly and felt completely personal.");
		form.setEventDate(LocalDate.now().minusDays(10));
		form.setConsentAccepted(true);
		return form;
	}

	@Test
	void sourceBackedJessicaReviewReplacesPublicDemoFallback(@TempDir Path tempDir) {
		ReviewService service = createService(tempDir);

		List<ReviewEntry> approved = service.getApprovedReviews();

		assertThat(approved).singleElement().satisfies(review -> {
			assertThat(review.getId()).isEqualTo(JESSICA_REVIEW_ID);
			assertThat(review.getReviewerName()).isEqualTo("Jessica");
			assertThat(review.getHeadline()).isEqualTo("The most special wedding day");
			assertThat(review.getEventDate()).isEqualTo(LocalDate.of(2026, 5, 24));
			assertThat(review.getEventDateDisplay()).isEqualTo("24 May 2026");
			assertThat(review.getRating()).isEqualTo(5);
		});
		assertThat(service.getApprovedReviews()).hasSize(1);
		assertThat(service.getManageableReviews()).isEmpty();
	}

	@Test
	void approvedPersistedCopyOfJessicaReviewIsNotDuplicated(@TempDir Path tempDir) {
		ReviewService service = createService(tempDir);
		ReviewForm form = validForm();
		form.setReviewerName(" Jessica ");
		form.setHeadline("The most special wedding day");
		form.setEventDate(LocalDate.of(2026, 5, 24));

		ReviewEntry persisted = service.submitReview(form, List.of());
		service.approveReview(persisted.getId(), "Imported with publication evidence");

		assertThat(service.getApprovedReviews()).singleElement()
				.extracting(ReviewEntry::getId)
				.isEqualTo(persisted.getId());
	}

	@Test
	void submitReviewStoresPendingReviewAndAllowsModeration(@TempDir Path tempDir) {
		ReviewService service = createService(tempDir);

		service.submitReview(validForm(), List.of());

		List<ReviewEntry> pending = service.getPendingReviews();
		assertThat(pending).hasSize(1);
		ReviewEntry pendingEntry = pending.get(0);
		assertThat(pendingEntry.getStatus()).isEqualTo(ReviewStatus.PENDING);
		assertThat(service.getApprovedReviews())
				.extracting(ReviewEntry::getId)
				.containsExactly(JESSICA_REVIEW_ID);

		service.approveReview(pendingEntry.getId(), "Verified genuine");

		List<ReviewEntry> approved = service.getApprovedReviews();
		assertThat(approved).hasSize(2);
		assertThat(approved).filteredOn(review -> review.getId().equals(pendingEntry.getId())).singleElement()
				.satisfies(review -> {
					assertThat(review.getStatus()).isEqualTo(ReviewStatus.APPROVED);
					assertThat(review.getModerationNote()).isEqualTo("Verified genuine");
				});
		assertThat(service.getApprovedFiveStarReviews()).hasSize(2);
		assertThat(service.getPendingReviews()).isEmpty();
	}

	@Test
	void rejectReviewDeletesReview(@TempDir Path tempDir) {
		ReviewService service = createService(tempDir);
		service.submitReview(validForm(), List.of());
		ReviewEntry pendingEntry = service.getPendingReviews().get(0);

		service.rejectReview(pendingEntry.getId(), "Insufficient detail");

		assertThat(service.getPendingReviews()).isEmpty();
		assertThat(service.getApprovedReviews())
				.extracting(ReviewEntry::getId)
				.containsExactly(JESSICA_REVIEW_ID);
		assertThat(service.getManageableReviews()).isEmpty();
	}

	@Test
	void manageableReviewsIncludesPendingSavedReviews(@TempDir Path tempDir) {
		ReviewService service = createService(tempDir);

		service.submitReview(validForm(), List.of());

		assertThat(service.getManageableReviews()).singleElement()
				.extracting(ReviewEntry::getStatus)
				.isEqualTo(ReviewStatus.PENDING);
	}

	@Test
	void deleteReviewRemovesSavedReview(@TempDir Path tempDir) {
		ReviewService service = createService(tempDir);
		service.submitReview(validForm(), List.of());
		ReviewEntry pendingEntry = service.getPendingReviews().get(0);
		service.approveReview(pendingEntry.getId(), "Verified genuine");

		service.deleteReview(pendingEntry.getId());

		assertThat(service.getApprovedReviews())
				.extracting(ReviewEntry::getId)
				.containsExactly(JESSICA_REVIEW_ID);
		assertThat(service.getManageableReviews()).isEmpty();
	}

	@Test
	void approvedReviewCanBeDisabledAndEnabled(@TempDir Path tempDir) {
		ReviewService service = createService(tempDir);
		service.submitReview(validForm(), List.of());
		ReviewEntry pendingEntry = service.getPendingReviews().get(0);
		service.approveReview(pendingEntry.getId(), "Verified genuine");

		service.disableReview(pendingEntry.getId());

		assertThat(service.getApprovedReviews())
				.extracting(ReviewEntry::getId)
				.containsExactly(JESSICA_REVIEW_ID);
		assertThat(service.getApprovedFiveStarReviews()).hasSize(1);
		assertThat(service.getManageableReviews()).singleElement()
				.extracting(ReviewEntry::getStatus)
				.isEqualTo(ReviewStatus.DISABLED);

		service.enableReview(pendingEntry.getId());

		assertThat(service.getApprovedReviews()).hasSize(2);
		assertThat(service.getManageableReviews()).singleElement()
				.extracting(ReviewEntry::getStatus)
				.isEqualTo(ReviewStatus.APPROVED);
	}

	@Test
	void submitReviewRejectsPhotoCountOverLimit(@TempDir Path tempDir) {
		ReviewService service = createService(tempDir);
		MockMultipartFile one = new MockMultipartFile("reviewPhotos", "one.jpg", "image/jpeg", new byte[] { 1 });
		MockMultipartFile two = new MockMultipartFile("reviewPhotos", "two.jpg", "image/jpeg", new byte[] { 2 });
		MockMultipartFile three = new MockMultipartFile("reviewPhotos", "three.jpg", "image/jpeg", new byte[] { 3 });

		assertThatThrownBy(() -> service.submitReview(validForm(), List.of(one, two, three)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Please upload up to 2 photos.");
	}

	@Test
	void submitReviewStoresPhotoAndResolvePhotoPathBlocksTraversal(@TempDir Path tempDir) {
		ReviewService service = createService(tempDir);
		MockMultipartFile photo = new MockMultipartFile("reviewPhotos", "wedding.jpg", "image/jpeg", new byte[] { 1, 2, 3 });

		service.submitReview(validForm(), List.of(photo));

		ReviewEntry pending = service.getPendingReviews().get(0);
		assertThat(pending.getPhotoFileNames()).hasSize(1);
		Path storedPath = service.resolvePhotoPath(pending.getPhotoFileNames().get(0));
		assertThat(storedPath).exists();

		assertThatThrownBy(() -> service.resolvePhotoPath("../../outside.jpg"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Invalid photo path.");
	}
}
