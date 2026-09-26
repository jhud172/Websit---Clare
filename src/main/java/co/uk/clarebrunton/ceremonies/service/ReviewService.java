package co.uk.clarebrunton.ceremonies.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import co.uk.clarebrunton.ceremonies.config.ReviewProperties;
import co.uk.clarebrunton.ceremonies.model.ReviewEntry;
import co.uk.clarebrunton.ceremonies.model.ReviewForm;
import co.uk.clarebrunton.ceremonies.model.ReviewStatus;
import co.uk.clarebrunton.ceremonies.repository.ReviewRepository;

@Service
public class ReviewService {

	private static final Set<String> IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "webp");
	private final ReviewProperties properties;
	private final ReviewRepository reviews;
	private final MediaStorage mediaStorage;
	private final UploadInspector uploadInspector;

	public ReviewService(ReviewProperties properties, ReviewRepository reviews,
			MediaStorage mediaStorage, UploadInspector uploadInspector) {
		this.properties = properties;
		this.reviews = reviews;
		this.mediaStorage = mediaStorage;
		this.uploadInspector = uploadInspector;
	}

	@Transactional(readOnly = true)
	public List<ReviewEntry> getApprovedReviews() {
		List<ReviewEntry> approved = new ArrayList<>(reviews.findByStatusOrderBySubmittedAtDesc(ReviewStatus.APPROVED));
		for (ReviewEntry curated : curatedApprovedReviews()) {
			boolean stableOverride = reviews.existsById(curated.getId());
			boolean duplicate = approved.stream().anyMatch(entry -> hasSameReviewFingerprint(entry, curated));
			if (!stableOverride && !duplicate) approved.add(0, curated);
		}
		return List.copyOf(approved);
	}

	@Transactional(readOnly = true)
	public List<ReviewEntry> getApprovedFiveStarReviews() {
		return getApprovedReviews().stream().filter(entry -> entry.getRating() == 5).toList();
	}

	@Transactional(readOnly = true)
	public List<ReviewEntry> getPendingReviews() {
		return reviews.findByStatusOrderBySubmittedAtDesc(ReviewStatus.PENDING);
	}

	@Transactional(readOnly = true)
	public List<ReviewEntry> getManageableReviews() {
		return reviews.findAllByOrderBySubmittedAtDesc();
	}

	public ReviewEntry submitReview(ReviewForm form, List<MultipartFile> photos) {
		String submissionId = validSubmissionId(form.getSubmissionToken());
		var existing = reviews.findById(submissionId);
		if (existing.isPresent()) { existing.get().setNewlySubmitted(false); return existing.get(); }
		List<MultipartFile> safePhotos = photos == null ? List.of() : photos.stream().filter(file -> file != null && !file.isEmpty()).toList();
		if (safePhotos.size() > properties.getMaxPhotoCount()) {
			throw new IllegalArgumentException("Please upload up to " + properties.getMaxPhotoCount() + " photos.");
		}
		for (MultipartFile photo : safePhotos) {
			if (photo.getSize() > properties.getMaxPhotoSizeBytes()) {
				throw new IllegalArgumentException("Each photo must be 5 MB or smaller.");
			}
		}
		List<UploadInspector.InspectedUpload> inspectedPhotos = safePhotos.stream()
				.map(photo -> uploadInspector.inspect(photo, IMAGE_TYPES))
				.toList();

		ReviewEntry entry = new ReviewEntry();
		entry.setId(submissionId);
		entry.setReviewerName(form.getReviewerName().trim());
		entry.setReviewerRole(clean(form.getReviewerRole()));
		entry.setCeremonyType(form.getCeremonyType().trim());
		entry.setRating(form.getRating());
		entry.setHeadline(clean(form.getHeadline()));
		entry.setMessage(form.getMessage().trim());
		entry.setEventDate(form.getEventDate());
		entry.setStatus(ReviewStatus.PENDING);
		entry.setSubmittedAt(OffsetDateTime.now());

		List<String> keys = new ArrayList<>();
		boolean persisted = false;
		try {
			for (var inspected : inspectedPhotos) {
				keys.add(mediaStorage.store("reviews/" + entry.getId(), inspected.originalName(), inspected.contentType(), inspected.content()));
			}
			entry.setPhotoFileNames(keys);
			ReviewEntry saved = reviews.save(entry);
			saved.setNewlySubmitted(true);
			persisted = true;
			return saved;
		}
		catch (RuntimeException exception) {
			if (!persisted) {
				keys.forEach(this::deleteQuietly);
			}
			throw exception;
		}
	}

	@Transactional
	public ReviewEntry approveReview(String id, String note) { return moderate(id, ReviewStatus.APPROVED, note); }

	@Transactional
	public void rejectReview(String id, String note) { deleteReview(id); }

	@Transactional
	public void deleteReview(String id) {
		ReviewEntry entry = find(id);
		entry.getPhotoFileNames().forEach(mediaStorage::delete);
		reviews.delete(entry);
	}

	@Transactional
	public void enableReview(String id) { moderate(id, ReviewStatus.APPROVED, "Enabled for public display."); }

	@Transactional
	public void disableReview(String id) { moderate(id, ReviewStatus.DISABLED, "Disabled from public display."); }

	public java.util.Optional<MediaStorage.StoredMedia> loadPhoto(String key) {
		if (!StringUtils.hasText(key) || key.contains("..") || !key.startsWith("reviews-")) return java.util.Optional.empty();
		return mediaStorage.load(key);
	}

	private String validSubmissionId(String token) {
		try {
			return UUID.fromString(token).toString();
		} catch (IllegalArgumentException | NullPointerException invalid) {
			return UUID.randomUUID().toString();
		}
	}

	private ReviewEntry moderate(String id, ReviewStatus status, String note) {
		ReviewEntry entry = find(id);
		entry.setStatus(status);
		entry.setModerationNote(clean(note));
		entry.setModeratedAt(OffsetDateTime.now());
		return reviews.save(entry);
	}

	private ReviewEntry find(String id) {
		return reviews.findById(id).orElseThrow(() -> new IllegalArgumentException("Review not found."));
	}

	private String clean(String value) { return StringUtils.hasText(value) ? value.trim() : null; }

	private void deleteQuietly(String assetKey) {
		try {
			mediaStorage.delete(assetKey);
		}
		catch (RuntimeException ignored) {
			// Preserve the original submission error; orphan cleanup can be retried operationally.
		}
	}

	private List<ReviewEntry> curatedApprovedReviews() {
		ReviewEntry entry = new ReviewEntry();
		entry.setId("client-review-jessica-wedding-2026-05-24");
		entry.setReviewerName("Jessica");
		entry.setReviewerRole("Wedding couple");
		entry.setCeremonyType("Wedding ceremony");
		entry.setRating(5);
		entry.setHeadline("The most special wedding day");
		entry.setMessage("""
				What a fantastic wedding day delivered by the wonderful Clare. We couldn't have asked for a more wonderful celebrant to marry us. From our very first meeting, Clare took time to truly understand our story and what made our relationship special helping create a ceremony that felt completely personal and meaningful.

				On the day itself, everything was delivered perfectly. Standing in the glorious sunshine marrying my best friend, was a moment we'll cherish forever and Clare played such a huge part in making it so memorable. Her warmth, professionalism and heartfelt delivery set exactly the right tone and kept everyone engaged throughout.

				The ceremony was beautifully written and presented, striking the perfect balance between emotion, laughter, and love. In fact the whole day was so moving that it had my mum in tears more than once!

				We are incredibly grateful for the care, attention and passion that went into making our wedding ceremony so special. If you're looking for a celebrant who will create a truly unforgettable experience, we cannot recommend Clare highly enough.
				""".strip());
		entry.setEventDate(LocalDate.of(2026, 5, 24));
		entry.setStatus(ReviewStatus.APPROVED);
		entry.setModerationNote("Client-supplied testimonial included in the website content source.");
		entry.setPhotoFileNames(List.of());
		return List.of(entry);
	}

	private boolean hasSameReviewFingerprint(ReviewEntry first, ReviewEntry second) {
		return normalise(first.getReviewerName()).equals(normalise(second.getReviewerName()))
				&& first.getEventDate() != null && first.getEventDate().equals(second.getEventDate())
				&& normalise(first.getHeadline()).equals(normalise(second.getHeadline()));
	}

	private String normalise(String value) {
		return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
	}
}
