package co.uk.clarebrunton.ceremonies.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewEntry {

	private String id;

	private String reviewerName;

	private String reviewerRole;

	private String ceremonyType;

	private int rating;

	private String headline;

	private String message;

	private LocalDate eventDate;

	private ReviewStatus status;

	private String moderationNote;

	private OffsetDateTime submittedAt;

	private OffsetDateTime moderatedAt;

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

}