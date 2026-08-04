package co.uk.clarebrunton.ceremonies.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.uk.clarebrunton.ceremonies.config.ReviewProperties;
import co.uk.clarebrunton.ceremonies.model.ReviewEntry;
import co.uk.clarebrunton.ceremonies.model.ReviewForm;
import co.uk.clarebrunton.ceremonies.model.ReviewStatus;

@Service
public class ReviewService {

	private static final String DATA_FILE_NAME = "reviews.json";

	private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

	private static final TypeReference<List<ReviewEntry>> REVIEW_LIST_TYPE = new TypeReference<>() {
	};

	private final ReviewProperties reviewProperties;

	private final ObjectMapper objectMapper;

	public ReviewService(ReviewProperties reviewProperties) {
		this.reviewProperties = reviewProperties;
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
	}

	public synchronized List<ReviewEntry> getApprovedReviews() {
		List<ReviewEntry> persistedReviews = loadAll();
		List<ReviewEntry> approvedReviews = new ArrayList<>(persistedReviews.stream()
				.filter(entry -> entry.getStatus() == ReviewStatus.APPROVED)
				.sorted(Comparator.comparing(ReviewEntry::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
				.toList());

		for (ReviewEntry curatedReview : curatedApprovedReviews()) {
			boolean hasStableIdOverride = persistedReviews.stream()
					.anyMatch(entry -> curatedReview.getId().equals(entry.getId()));
			boolean hasApprovedDuplicate = approvedReviews.stream()
					.anyMatch(entry -> hasSameReviewFingerprint(entry, curatedReview));

			if (!hasStableIdOverride && !hasApprovedDuplicate) {
				approvedReviews.add(0, curatedReview);
			}
		}

		return List.copyOf(approvedReviews);
	}

	public synchronized List<ReviewEntry> getApprovedFiveStarReviews() {
		return getApprovedReviews().stream()
				.filter(entry -> entry.getRating() == 5)
				.toList();
	}

	public synchronized List<ReviewEntry> getPendingReviews() {
		return loadAll().stream()
				.filter(entry -> entry.getStatus() == ReviewStatus.PENDING)
				.sorted(Comparator.comparing(ReviewEntry::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
				.toList();
	}

	public synchronized List<ReviewEntry> getManageableReviews() {
		return loadAll().stream()
				.sorted(Comparator.comparing(ReviewEntry::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
				.toList();
	}

	public synchronized ReviewEntry submitReview(ReviewForm form, List<MultipartFile> photos) {
		List<MultipartFile> safePhotos = normalisePhotos(photos);
		String validationError = validatePhotos(safePhotos);

		if (validationError != null) {
			throw new IllegalArgumentException(validationError);
		}

		List<ReviewEntry> entries = loadAll();
		ReviewEntry entry = new ReviewEntry();
		entry.setId(UUID.randomUUID().toString());
		entry.setReviewerName(form.getReviewerName().trim());
		entry.setReviewerRole(cleanNullable(form.getReviewerRole()));
		entry.setCeremonyType(form.getCeremonyType().trim());
		entry.setRating(form.getRating());
		entry.setHeadline(cleanNullable(form.getHeadline()));
		entry.setMessage(form.getMessage().trim());
		entry.setEventDate(form.getEventDate());
		entry.setStatus(ReviewStatus.PENDING);
		entry.setSubmittedAt(OffsetDateTime.now());
		entry.setPhotoFileNames(storePhotos(safePhotos));

		entries.add(entry);
		saveAll(entries);
		return entry;
	}

	public synchronized ReviewEntry approveReview(String reviewId, String note) {
		return moderate(reviewId, ReviewStatus.APPROVED, note);
	}

	public synchronized void rejectReview(String reviewId, String note) {
		deleteReview(reviewId);
	}

	public synchronized void deleteReview(String reviewId) {
		List<ReviewEntry> entries = loadAll();
		ReviewEntry matched = findReview(entries, reviewId);
		entries.remove(matched);
		deletePhotos(matched.getPhotoFileNames());
		saveAll(entries);
	}

	public synchronized void enableReview(String reviewId) {
		moderate(reviewId, ReviewStatus.APPROVED, "Enabled for public display.");
	}

	public synchronized void disableReview(String reviewId) {
		moderate(reviewId, ReviewStatus.DISABLED, "Disabled from public display.");
	}

	public Path resolvePhotoPath(String filename) {
		Path root = getPhotoDirectoryPath();
		Path resolved = root.resolve(filename).normalize();

		if (!resolved.startsWith(root)) {
			throw new IllegalArgumentException("Invalid photo path.");
		}

		return resolved;
	}

	private ReviewEntry moderate(String reviewId, ReviewStatus status, String note) {
		List<ReviewEntry> entries = loadAll();
		ReviewEntry matched = findReview(entries, reviewId);

		matched.setStatus(status);
		matched.setModerationNote(cleanNullable(note));
		matched.setModeratedAt(OffsetDateTime.now());
		saveAll(entries);
		return matched;
	}

	private ReviewEntry findReview(List<ReviewEntry> entries, String reviewId) {
		return entries.stream()
				.filter(entry -> entry.getId().equals(reviewId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Review not found."));
	}

	private void deletePhotos(List<String> photoFileNames) {
		if (photoFileNames == null || photoFileNames.isEmpty()) {
			return;
		}

		for (String photoFileName : photoFileNames) {
			if (!StringUtils.hasText(photoFileName) || photoFileName.startsWith("/")) {
				continue;
			}

			try {
				Files.deleteIfExists(resolvePhotoPath(photoFileName));
			}
			catch (IOException exception) {
				throw new IllegalStateException("Unable to delete rejected review image.", exception);
			}
		}
	}

	private List<ReviewEntry> loadAll() {
		Path file = getDataFilePath();

		if (!Files.exists(file)) {
			return new ArrayList<>();
		}

		try {
			return objectMapper.readValue(file.toFile(), REVIEW_LIST_TYPE);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Unable to read reviews data.", exception);
		}
	}

	private void saveAll(List<ReviewEntry> entries) {
		Path file = getDataFilePath();

		try {
			Files.createDirectories(file.getParent());
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), entries);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Unable to save reviews data.", exception);
		}
	}

	private List<String> storePhotos(List<MultipartFile> photos) {
		if (photos.isEmpty()) {
			return List.of();
		}

		Path photoDir = getPhotoDirectoryPath();
		List<String> storedNames = new ArrayList<>();

		try {
			Files.createDirectories(photoDir);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Unable to create review image directory.", exception);
		}

		for (MultipartFile photo : photos) {
			String extension = getExtension(photo.getOriginalFilename());
			String filename = UUID.randomUUID() + "." + extension;
			Path destination = photoDir.resolve(filename);

			try (InputStream inputStream = photo.getInputStream()) {
				Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException exception) {
				throw new IllegalStateException("Unable to store uploaded review image.", exception);
			}

			storedNames.add(filename);
		}

		return storedNames;
	}

	private List<MultipartFile> normalisePhotos(List<MultipartFile> photos) {
		if (photos == null) {
			return List.of();
		}

		return photos.stream()
				.filter(file -> file != null && !file.isEmpty())
				.toList();
	}

	private String validatePhotos(List<MultipartFile> photos) {
		if (photos.size() > reviewProperties.getMaxPhotoCount()) {
			return "Please upload up to " + reviewProperties.getMaxPhotoCount() + " photos.";
		}

		for (MultipartFile photo : photos) {
			if (photo.getSize() > reviewProperties.getMaxPhotoSizeBytes()) {
				return "Each photo must be 5 MB or smaller.";
			}

			String extension = getExtension(photo.getOriginalFilename());
			if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
				return "Please upload JPG, PNG or WEBP images only.";
			}
		}

		return null;
	}

	private String getExtension(String originalName) {
		String extension = StringUtils.getFilenameExtension(originalName);
		if (!StringUtils.hasText(extension)) {
			return "";
		}
		return extension.toLowerCase(Locale.ROOT);
	}

	private Path getDataFilePath() {
		return Path.of(reviewProperties.getStorageDirectory()).resolve(DATA_FILE_NAME).toAbsolutePath().normalize();
	}

	private Path getPhotoDirectoryPath() {
		return Path.of(reviewProperties.getStorageDirectory())
				.resolve(reviewProperties.getPhotoDirectory())
				.toAbsolutePath()
				.normalize();
	}

	private String cleanNullable(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
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
		return normaliseFingerprintText(first.getReviewerName()).equals(normaliseFingerprintText(second.getReviewerName()))
				&& first.getEventDate() != null
				&& first.getEventDate().equals(second.getEventDate())
				&& normaliseFingerprintText(first.getHeadline()).equals(normaliseFingerprintText(second.getHeadline()));
	}

	private String normaliseFingerprintText(String value) {
		return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
	}

}
