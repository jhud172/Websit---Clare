package co.uk.clarebrunton.ceremonies.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
		return loadAll().stream()
				.filter(entry -> entry.getStatus() == ReviewStatus.APPROVED)
				.sorted(Comparator.comparing(ReviewEntry::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
				.toList();
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

	public synchronized void submitReview(ReviewForm form, List<MultipartFile> photos) {
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
	}

	public synchronized void approveReview(String reviewId, String note) {
		moderate(reviewId, ReviewStatus.APPROVED, note);
	}

	public synchronized void rejectReview(String reviewId, String note) {
		moderate(reviewId, ReviewStatus.REJECTED, note);
	}

	public Path resolvePhotoPath(String filename) {
		Path root = getPhotoDirectoryPath();
		Path resolved = root.resolve(filename).normalize();

		if (!resolved.startsWith(root)) {
			throw new IllegalArgumentException("Invalid photo path.");
		}

		return resolved;
	}

	private void moderate(String reviewId, ReviewStatus status, String note) {
		List<ReviewEntry> entries = loadAll();
		ReviewEntry matched = entries.stream()
				.filter(entry -> entry.getId().equals(reviewId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Review not found."));

		matched.setStatus(status);
		matched.setModerationNote(cleanNullable(note));
		matched.setModeratedAt(OffsetDateTime.now());
		saveAll(entries);
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

}