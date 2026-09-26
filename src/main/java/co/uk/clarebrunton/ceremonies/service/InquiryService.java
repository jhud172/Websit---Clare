package co.uk.clarebrunton.ceremonies.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import co.uk.clarebrunton.ceremonies.model.InquiryForm;
import co.uk.clarebrunton.ceremonies.model.InquiryRecord;
import co.uk.clarebrunton.ceremonies.model.InquiryStatus;
import co.uk.clarebrunton.ceremonies.repository.InquiryRepository;

@Service
public class InquiryService {

	private static final Set<String> FILE_TYPES = Set.of("jpg", "jpeg", "png", "webp", "pdf");
	private final InquiryRepository inquiries;
	private final MediaStorage mediaStorage;
	private final UploadInspector uploadInspector;
	private final InquiryNotificationService notifications;

	public InquiryService(InquiryRepository inquiries, MediaStorage mediaStorage,
			UploadInspector uploadInspector, InquiryNotificationService notifications) {
		this.inquiries = inquiries;
		this.mediaStorage = mediaStorage;
		this.uploadInspector = uploadInspector;
		this.notifications = notifications;
	}

	public InquiryRecord submit(InquiryForm form, List<MultipartFile> attachments) {
		String submissionId = validSubmissionId(form.getSubmissionToken());
		var existing = inquiries.findById(submissionId);
		if (existing.isPresent()) return existing.get();
		List<MultipartFile> suppliedAttachments = attachments == null ? List.of() : attachments.stream()
				.filter(attachment -> attachment != null && !attachment.isEmpty())
				.toList();
		if (suppliedAttachments.size() > 3) {
			throw new IllegalArgumentException("Please attach no more than three files.");
		}
		List<UploadInspector.InspectedUpload> inspectedAttachments = suppliedAttachments.stream()
				.map(attachment -> uploadInspector.inspect(attachment, FILE_TYPES))
				.toList();

		InquiryRecord record = new InquiryRecord();
		record.setId(submissionId);
		record.setFullName(clean(form.getFullName()));
		record.setEmail(clean(form.getEmail()));
		record.setPhone(clean(form.getPhone()));
		record.setServiceType(clean(form.getServiceType()));
		record.setEventDate(form.getEventDate());
		record.setDatePreference(clean(form.getDatePreference()));
		record.setVenue(clean(form.getVenue()));
		record.setMessage(clean(form.getMessage()));
		record.setPackageName(clean(form.getPackageName()));
		record.setSourcePage(clean(form.getSourcePage()));
		record.setStatus(InquiryStatus.NEW);
		record.setSubmittedAt(OffsetDateTime.now());
		record.setUpdatedAt(record.getSubmittedAt());

		List<String> keys = new ArrayList<>();
		boolean persisted = false;
		try {
			for (var inspected : inspectedAttachments) {
				keys.add(mediaStorage.store("inquiries/" + record.getId(), inspected.originalName(), inspected.contentType(), inspected.content()));
			}
			record.setAttachmentKeys(keys);
			InquiryRecord saved = inquiries.save(record);
			persisted = true;

			// The repository transaction commits before notification, so an email outage cannot lose the enquiry.
			notifications.handleInquiry(saved.getId(), form, suppliedAttachments);
			return saved;
		}
		catch (RuntimeException exception) {
			if (!persisted) {
				keys.forEach(this::deleteQuietly);
			}
			throw exception;
		}
	}

	private String validSubmissionId(String token) {
		try {
			return UUID.fromString(token).toString();
		} catch (IllegalArgumentException | NullPointerException invalid) {
			return UUID.randomUUID().toString();
		}
	}

	@Transactional(readOnly = true)
	public List<InquiryRecord> findAll() {
		return inquiries.findAllByOrderBySubmittedAtDesc();
	}

	@Transactional(readOnly = true)
	public List<InquiryRecord> findForDashboard(String query, InquiryStatus status, String service, String sort, boolean includeArchived) {
		String needle = clean(query);
		Comparator<InquiryRecord> comparator = "oldest".equalsIgnoreCase(sort)
				? Comparator.comparing(InquiryRecord::getSubmittedAt)
				: Comparator.comparing(InquiryRecord::getSubmittedAt).reversed();
		return inquiries.findAllByOrderBySubmittedAtDesc().stream()
				.filter(record -> includeArchived || record.getStatus() != InquiryStatus.ARCHIVED)
				.filter(record -> status == null || record.getStatus() == status)
				.filter(record -> !StringUtils.hasText(service) || service.equals(record.getServiceType()))
				.filter(record -> !StringUtils.hasText(needle) || searchable(record).contains(needle.toLowerCase()))
				.sorted(comparator)
				.toList();
	}

	@Transactional(readOnly = true)
	public Map<String, Long> statusCounts() {
		Map<String, Long> counts = new LinkedHashMap<>();
		for (InquiryStatus status : InquiryStatus.values()) counts.put(status.name(), inquiries.countByStatus(status));
		return counts;
	}

	@Transactional(readOnly = true)
	public List<String> serviceTypes() {
		return inquiries.findAllByOrderBySubmittedAtDesc().stream().map(InquiryRecord::getServiceType)
				.filter(StringUtils::hasText).distinct().sorted().toList();
	}

	@Transactional(readOnly = true)
	public java.util.Optional<MediaStorage.StoredMedia> loadAttachment(String inquiryId, String filename) {
		if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) return java.util.Optional.empty();
		return inquiries.findById(inquiryId).flatMap(record -> record.getAttachmentKeys().stream()
				.filter(key -> key.equals("inquiries-" + inquiryId + "/" + filename))
				.findFirst().flatMap(mediaStorage::load));
	}

	@Transactional(readOnly = true)
	public long countNew() {
		return inquiries.countByStatus(InquiryStatus.NEW);
	}

	@Transactional
	public InquiryRecord update(String id, InquiryStatus status, String notes) {
		InquiryRecord record = inquiries.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Enquiry not found."));
		record.setStatus(status);
		record.setAdminNotes(clean(notes));
		record.setUpdatedAt(OffsetDateTime.now());
		return inquiries.save(record);
	}

	private String clean(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private String searchable(InquiryRecord record) {
		return String.join(" ", nullSafe(record.getFullName()), nullSafe(record.getEmail()), nullSafe(record.getPhone()),
				nullSafe(record.getVenue()), nullSafe(record.getServiceType()), nullSafe(record.getPackageName())).toLowerCase();
	}

	private String nullSafe(String value) { return value == null ? "" : value; }

	private void deleteQuietly(String assetKey) {
		try {
			mediaStorage.delete(assetKey);
		}
		catch (RuntimeException ignored) {
			// Preserve the original submission error; orphan cleanup can be retried operationally.
		}
	}
}
