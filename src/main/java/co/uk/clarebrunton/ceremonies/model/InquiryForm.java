package co.uk.clarebrunton.ceremonies.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InquiryForm {

	@NotBlank(message = "Please add your full name.")
	private String fullName;

	@NotBlank(message = "Please add an email address.")
	@Email(message = "Please use a valid email address.")
	private String email;

	@Size(max = 24, message = "Please add a valid phone number.")
	private String phone;

	@NotBlank(message = "Please choose the type of ceremony.")
	private String serviceType;

	@FutureOrPresent(message = "Please choose a date that is today or later.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate eventDate;

	@Size(max = 180, message = "Please keep the venue or location under 180 characters.")
	private String venue;

	@Size(max = 2000, message = "Please keep the message under 2000 characters.")
	private String message;

	@Size(max = 120)
	private String packageName;

	@Size(max = 120)
	private String sourcePage;

	@Size(max = 24)
	private String datePreference;

	@Size(max = 120)
	private String website;

	private long formStartedAt;

	@Size(max = 36)
	private String submissionToken;

	@Size(max = 2048)
	private String turnstileResponse;

	@AssertTrue(message = "Please confirm that you are happy for us to handle your details.")
	private boolean privacyAccepted;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPackageName() { return packageName; }
	public void setPackageName(String packageName) { this.packageName = packageName; }
	public String getSourcePage() { return sourcePage; }
	public void setSourcePage(String sourcePage) { this.sourcePage = sourcePage; }
	public String getDatePreference() { return datePreference; }
	public void setDatePreference(String datePreference) { this.datePreference = datePreference; }
	public String getWebsite() { return website; }
	public void setWebsite(String website) { this.website = website; }
	public long getFormStartedAt() { return formStartedAt; }
	public void setFormStartedAt(long formStartedAt) { this.formStartedAt = formStartedAt; }
	public String getSubmissionToken() { return submissionToken; }
	public void setSubmissionToken(String submissionToken) { this.submissionToken = submissionToken; }
	public String getTurnstileResponse() { return turnstileResponse; }
	public void setTurnstileResponse(String turnstileResponse) { this.turnstileResponse = turnstileResponse; }

	public boolean isPrivacyAccepted() {
		return privacyAccepted;
	}

	public void setPrivacyAccepted(boolean privacyAccepted) {
		this.privacyAccepted = privacyAccepted;
	}

}
