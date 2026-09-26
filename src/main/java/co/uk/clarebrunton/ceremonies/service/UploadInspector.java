package co.uk.clarebrunton.ceremonies.service;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UploadInspector {

	private static final Map<String, String> MIME_TYPES = Map.of(
			"jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp", "pdf", "application/pdf");

	public InspectedUpload inspect(MultipartFile file, Set<String> permittedExtensions) {
		String extension = extension(file.getOriginalFilename());
		if (!permittedExtensions.contains(extension)) {
			throw new IllegalArgumentException("This file type is not supported.");
		}
		try {
			byte[] content = file.getBytes();
			if (!matchesSignature(extension, content)) {
				throw new IllegalArgumentException("A file did not match its filename type. Please upload the original file.");
			}
			return new InspectedUpload(safeName(file.getOriginalFilename()), MIME_TYPES.get(extension), content);
		}
		catch (IOException exception) {
			throw new IllegalArgumentException("A file could not be read. Please try attaching it again.", exception);
		}
	}

	private boolean matchesSignature(String extension, byte[] bytes) {
		return switch (extension) {
			case "jpg", "jpeg" -> starts(bytes, 0xFF, 0xD8, 0xFF);
			case "png" -> starts(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
			case "pdf" -> starts(bytes, 0x25, 0x50, 0x44, 0x46, 0x2D);
			case "webp" -> bytes.length >= 12 && starts(bytes, 0x52, 0x49, 0x46, 0x46)
					&& bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50;
			default -> false;
		};
	}

	private boolean starts(byte[] bytes, int... signature) {
		if (bytes.length < signature.length) return false;
		for (int index = 0; index < signature.length; index++) {
			if ((bytes[index] & 0xFF) != signature[index]) return false;
		}
		return true;
	}

	private String extension(String name) {
		String value = StringUtils.getFilenameExtension(name);
		return StringUtils.hasText(value) ? value.toLowerCase(Locale.ROOT) : "";
	}

	private String safeName(String name) {
		return StringUtils.hasText(name) ? java.nio.file.Path.of(name).getFileName().toString() : "upload";
	}

	public record InspectedUpload(String originalName, String contentType, byte[] content) { }
}
