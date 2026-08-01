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
		boolean hasReviewDataFile = Files.exists(getDataFilePath());
		List<ReviewEntry> approvedReviews = loadAll().stream()
				.filter(entry -> entry.getStatus() == ReviewStatus.APPROVED)
				.sorted(Comparator.comparing(ReviewEntry::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
				.toList();

		return approvedReviews.isEmpty() && !hasReviewDataFile ? demoApprovedReviews() : approvedReviews;
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
		ensureInitialReviewData();
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

	private void ensureInitialReviewData() {
		if (Files.exists(getDataFilePath())) {
			return;
		}
		saveAll(new ArrayList<>(demoApprovedReviews()));
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

	private List<ReviewEntry> demoApprovedReviews() {
		return List.of(
				demoReview(
						"demo-review-wedding-emily-tom",
						"Emily and Tom",
						"Wedding couple",
						"Wedding ceremony",
						"A ceremony that felt completely like us",
						"Clare brought such warmth, calm and personality to our wedding ceremony. The script felt beautifully written, relaxed and full of the little details that mattered to us. So many guests said it was the most personal ceremony they had been part of.",
						LocalDate.of(2026, 5, 2),
						OffsetDateTime.parse("2026-05-12T10:30:00+01:00"),
						List.of("/images/weddings/amy-back-off-head-2.jpg", "/images/weddings/amy-back-off-head-1.jpg")
				),
				demoReview(
						"demo-review-wedding-laura-ben",
						"Laura and Ben",
						"Wedding couple",
						"Wedding ceremony",
						"Warm, polished and so thoughtful",
						"From the first conversation Clare made everything feel easy. She listened carefully, helped us shape the tone, and delivered a ceremony that was modern, sincere and full of joy.",
						LocalDate.of(2026, 4, 18),
						OffsetDateTime.parse("2026-05-08T14:15:00+01:00"),
						List.of("/images/weddings/detail-rings.jpg")
				),
				demoReview(
						"demo-review-funeral-henderson-family",
						"The Henderson family",
						"Family member",
						"Celebration of Life or memorial",
						"A tribute full of care and dignity",
						"Clare handled a difficult day with real compassion. She took time to understand Dad's life, his humour and what mattered to us, then created a tribute that felt gentle, dignified and deeply personal.",
						LocalDate.of(2026, 3, 27),
						OffsetDateTime.parse("2026-04-20T09:40:00+01:00"),
						List.of("/images/funerals/memorial-flowers-detail.jpg")
				),
				demoReview(
						"demo-review-venue-rachel",
						"Rachel",
						"Venue coordinator",
						"Wedding ceremony",
						"Professional from start to finish",
						"Clare was calm, organised and a pleasure to work with on the day. She gave us the space to enjoy the moment while making sure the ceremony flowed beautifully.",
						LocalDate.of(2026, 2, 14),
						OffsetDateTime.parse("2026-04-04T16:20:00+01:00"),
						List.of("/images/clare/wedding-symbolism-ribbons.jpg")
				)
		);
	}

	private ReviewEntry demoReview(String id, String reviewerName, String reviewerRole, String ceremonyType,
			String headline, String message, LocalDate eventDate, OffsetDateTime submittedAt, List<String> photoFileNames) {
		ReviewEntry entry = new ReviewEntry();
		entry.setId(id);
		entry.setReviewerName(reviewerName);
		entry.setReviewerRole(reviewerRole);
		entry.setCeremonyType(ceremonyType);
		entry.setRating(5);
		entry.setHeadline(headline);
		entry.setMessage(message);
		entry.setEventDate(eventDate);
		entry.setStatus(ReviewStatus.APPROVED);
		entry.setModerationNote("Demo review shown until real approved reviews are available.");
		entry.setSubmittedAt(submittedAt);
		entry.setModeratedAt(submittedAt);
		entry.setPhotoFileNames(photoFileNames);
		return entry;
	}

}
