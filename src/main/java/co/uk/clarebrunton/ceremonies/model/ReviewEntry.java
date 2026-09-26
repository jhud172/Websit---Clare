package co.uk.clarebrunton.ceremonies.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.Transient;

@Entity
@Table(name = "reviews")
public class ReviewEntry {
	@Transient
	private boolean newlySubmitted;

	private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.UK);

	@Id
	private String id;

	@Column(nullable = false)
	private String reviewerName;

	private String reviewerRole;

	@Column(nullable = false)
	private String ceremonyType;

	@Column(nullable = false)
	private int rating;

	private String headline;

	@Column(nullable = false, length = 2000)
	private String message;

	private LocalDate eventDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private ReviewStatus status;

	private String moderationNote;

	@Column(nullable = false)
	private OffsetDateTime submittedAt;

	private OffsetDateTime moderatedAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "review_photos", joinColumns = @JoinColumn(name = "review_id"))
	@Column(name = "asset_key", nullable = false)
	private List<String> photoFileNames = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReviewerName() {
		return reviewerName;
	}

	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
	}

	public String getReviewerRole() {
		return reviewerRole;
	}

	public void setReviewerRole(String reviewerRole) {
		this.reviewerRole = reviewerRole;
	}

	public String getCeremonyType() {
		return ceremonyType;
	}

	public void setCeremonyType(String ceremonyType) {
		this.ceremonyType = ceremonyType;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}

	@JsonIgnore
	public String getEventDateDisplay() {
		return eventDate == null ? null : eventDate.format(EVENT_DATE_FORMATTER);
	}

	public ReviewStatus getStatus() {
		return status;
	}

	public void setStatus(ReviewStatus status) {
		this.status = status;
	}

	public String getModerationNote() {
		return moderationNote;
	}

	public void setModerationNote(String moderationNote) {
		this.moderationNote = moderationNote;
	}

	public OffsetDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(OffsetDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public OffsetDateTime getModeratedAt() {
		return moderatedAt;
	}

	public void setModeratedAt(OffsetDateTime moderatedAt) {
		this.moderatedAt = moderatedAt;
	}

	public List<String> getPhotoFileNames() {
		return photoFileNames;
	}

	public void setPhotoFileNames(List<String> photoFileNames) {
		this.photoFileNames = photoFileNames;
	}
	public boolean isNewlySubmitted() { return newlySubmitted; }
	public void setNewlySubmitted(boolean newlySubmitted) { this.newlySubmitted = newlySubmitted; }

}
