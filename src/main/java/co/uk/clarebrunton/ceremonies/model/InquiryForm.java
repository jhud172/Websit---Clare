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

	@NotBlank(message = "Please add a phone number.")
	@Size(min = 7, max = 24, message = "Please add a valid phone number.")
	private String phone;

	@NotBlank(message = "Please choose the type of ceremony.")
	private String serviceType;

	@NotNull(message = "Please add a preferred date.")
	@FutureOrPresent(message = "Please choose a date that is today or later.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate eventDate;

	@NotBlank(message = "Please add the venue or location.")
	private String venue;

	@NotBlank(message = "Please tell us a little about the ceremony.")
	@Size(min = 20, max = 2000, message = "Please give between 20 and 2000 characters.")
	private String message;

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

	public boolean isPrivacyAccepted() {
		return privacyAccepted;
	}

	public void setPrivacyAccepted(boolean privacyAccepted) {
		this.privacyAccepted = privacyAccepted;
	}

}
