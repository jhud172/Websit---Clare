package co.uk.clarebrunton.ceremonies.service;

import java.util.Optional;

public interface MediaStorage {

	String store(String category, String originalName, String contentType, byte[] content);
	Optional<StoredMedia> load(String assetKey);
	void delete(String assetKey);

	record StoredMedia(String assetKey, String originalName, String contentType, byte[] content) { }
}
