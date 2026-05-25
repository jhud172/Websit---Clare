package co.uk.clarebrunton.ceremonies.model;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewForm {

	@NotBlank(message = "Please add your name.")
	@Size(max = 100, message = "Please keep your name under 100 characters.")
	private String reviewerName;

	@Size(max = 100, message = "Please keep this under 100 characters.")
	private String reviewerRole;

	@NotBlank(message = "Please select a ceremony type.")
	private String ceremonyType;

	@NotNull(message = "Please select a star rating.")
	@Min(value = 1, message = "Rating must be between 1 and 5.")
	@Max(value = 5, message = "Rating must be between 1 and 5.")
	private Integer rating;

	@Size(max = 50, message = "Please keep the heading under 50 characters.")
	private String headline;

	@NotBlank(message = "Please share your review.")
	@Size(min = 30, max = 2000, message = "Please write between 30 and 2000 characters.")
	private String message;

	private LocalDate eventDate;

	@AssertTrue(message = "Please confirm you are happy for this review to be considered for publication.")
	private boolean consentAccepted;

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

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
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

	public boolean isConsentAccepted() {
		return consentAccepted;
	}

	public void setConsentAccepted(boolean consentAccepted) {
		this.consentAccepted = consentAccepted;
	}

}
