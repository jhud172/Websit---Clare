package co.uk.clarebrunton.ceremonies.service;

import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResendEmailClient {

	private final RestClient restClient;

	@Value("${email.resend.api-key:}")
	private String apiKey;

	@Value("${email.resend.from-email:}")
	private String fromEmail;

	public ResendEmailClient() {
		this.restClient = RestClient.builder().baseUrl("https://api.resend.com").build();
	}

	public boolean isConfigured() {
		return StringUtils.hasText(apiKey) && StringUtils.hasText(fromEmail);
	}

	public void send(String to,
			String replyTo,
			String subject,
			String plainText,
			String html,
			List<MultipartFile> attachments) {
		if (!isConfigured()) {
			throw new IllegalStateException("Resend email client is not configured.");
		}

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("from", fromEmail);
		payload.put("to", List.of(to));
		if (StringUtils.hasText(replyTo)) {
			payload.put("reply_to", replyTo);
		}
		payload.put("subject", subject);
		payload.put("text", plainText);
		payload.put("html", html);

		List<Map<String, String>> encodedAttachments = encodeAttachments(attachments);
		if (!encodedAttachments.isEmpty()) {
			payload.put("attachments", encodedAttachments);
		}

		try {
			restClient.post()
					.uri("/emails")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(payload)
					.retrieve()
					.toBodilessEntity();
		}
		catch (RestClientException exception) {
			throw new EmailDeliveryException("Resend API email request failed.", exception);
		}
	}

	private List<Map<String, String>> encodeAttachments(List<MultipartFile> attachments) {
		if (attachments == null || attachments.isEmpty()) {
			return List.of();
		}

		return attachments.stream()
				.map(this::encodeAttachment)
				.toList();
	}

	private Map<String, String> encodeAttachment(MultipartFile attachment) {
		try {
			String filename = StringUtils.hasText(attachment.getOriginalFilename())
					? attachment.getOriginalFilename()
					: "attachment";

			Map<String, String> encoded = new LinkedHashMap<>();
			encoded.put("filename", filename);
			encoded.put("content", Base64.getEncoder().encodeToString(attachment.getBytes()));
			if (StringUtils.hasText(attachment.getContentType())) {
				encoded.put("content_type", attachment.getContentType());
			}
			return encoded;
		}
		catch (IOException exception) {
			throw new EmailDeliveryException("Could not encode email attachment.", exception);
		}
	}

	public static class EmailDeliveryException extends RuntimeException {

		public EmailDeliveryException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
