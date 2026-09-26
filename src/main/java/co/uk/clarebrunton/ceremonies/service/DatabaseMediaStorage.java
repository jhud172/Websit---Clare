package co.uk.clarebrunton.ceremonies.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import co.uk.clarebrunton.ceremonies.model.StoredAsset;
import co.uk.clarebrunton.ceremonies.repository.StoredAssetRepository;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "database", matchIfMissing = true)
public class DatabaseMediaStorage implements MediaStorage {

	private final StoredAssetRepository assets;

	public DatabaseMediaStorage(StoredAssetRepository assets) {
		this.assets = assets;
	}

	@Override
	@Transactional
	public String store(String category, String originalName, String contentType, byte[] content) {
		String extension = StringUtils.getFilenameExtension(originalName);
		String key = safeCategory(category) + "/" + UUID.randomUUID()
				+ (StringUtils.hasText(extension) ? "." + extension.toLowerCase() : "");
		StoredAsset asset = new StoredAsset();
		asset.setAssetKey(key);
		asset.setOriginalName(cleanName(originalName));
		asset.setContentType(contentType);
		asset.setSizeBytes(content.length);
		asset.setCreatedAt(OffsetDateTime.now());
		asset.setContent(content.clone());
		assets.save(asset);
		return key;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StoredMedia> load(String assetKey) {
		return assets.findById(assetKey)
				.map(asset -> new StoredMedia(asset.getAssetKey(), asset.getOriginalName(), asset.getContentType(), asset.getContent()));
	}

	@Override
	@Transactional
	public void delete(String assetKey) {
		assets.deleteById(assetKey);
	}

	private String safeCategory(String category) {
		return StringUtils.hasText(category) ? category.replaceAll("[^a-zA-Z0-9_-]", "-") : "media";
	}

	private String cleanName(String originalName) {
		if (!StringUtils.hasText(originalName)) {
			return "upload";
		}
		return java.nio.file.Path.of(originalName).getFileName().toString();
	}
}
