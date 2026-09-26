package co.uk.clarebrunton.ceremonies.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "inquiries")
public class InquiryRecord {

	@Id
	private String id;
	@Column(nullable = false)
	private String fullName;
	@Column(nullable = false)
	private String email;
	private String phone;
	@Column(nullable = false)
	private String serviceType;
	private LocalDate eventDate;
	private String datePreference;
	private String venue;
	@Column(length = 2000)
	private String message;
	private String packageName;
	private String sourcePage;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private InquiryStatus status;
	@Column(length = 2000)
	private String adminNotes;
	@Column(nullable = false)
	private OffsetDateTime submittedAt;
	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "inquiry_attachments", joinColumns = @JoinColumn(name = "inquiry_id"))
	@Column(name = "asset_key", nullable = false)
	private List<String> attachmentKeys = new ArrayList<>();

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getServiceType() { return serviceType; }
	public void setServiceType(String serviceType) { this.serviceType = serviceType; }
	public LocalDate getEventDate() { return eventDate; }
	public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
	public String getDatePreference() { return datePreference; }
	public void setDatePreference(String datePreference) { this.datePreference = datePreference; }
	public String getVenue() { return venue; }
	public void setVenue(String venue) { this.venue = venue; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public String getPackageName() { return packageName; }
	public void setPackageName(String packageName) { this.packageName = packageName; }
	public String getSourcePage() { return sourcePage; }
	public void setSourcePage(String sourcePage) { this.sourcePage = sourcePage; }
	public InquiryStatus getStatus() { return status; }
	public void setStatus(InquiryStatus status) { this.status = status; }
	public String getAdminNotes() { return adminNotes; }
	public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
	public OffsetDateTime getSubmittedAt() { return submittedAt; }
	public void setSubmittedAt(OffsetDateTime submittedAt) { this.submittedAt = submittedAt; }
	public OffsetDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
	public List<String> getAttachmentKeys() { return attachmentKeys; }
	public void setAttachmentKeys(List<String> attachmentKeys) { this.attachmentKeys = attachmentKeys; }
	public boolean isFollowUpOverdue() {
		return (status == InquiryStatus.NEW || status == InquiryStatus.CONTACTED)
				&& submittedAt != null && submittedAt.isBefore(OffsetDateTime.now().minusDays(2));
	}
}
