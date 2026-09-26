package co.uk.clarebrunton.ceremonies.service;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.uk.clarebrunton.ceremonies.config.StorageProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3MediaStorage implements MediaStorage {

	private final StorageProperties properties;
	private final S3Client client;

	public S3MediaStorage(StorageProperties properties) {
		this.properties = properties;
		if (!StringUtils.hasText(properties.getBucket())
				|| !StringUtils.hasText(properties.getAccessKey())
				|| !StringUtils.hasText(properties.getSecretKey())) {
			throw new IllegalStateException("S3 media storage requires bucket, access key and secret key settings.");
		}
		var builder = S3Client.builder()
				.region(Region.of(properties.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
				.forcePathStyle(StringUtils.hasText(properties.getEndpoint()));
		if (StringUtils.hasText(properties.getEndpoint())) {
			builder.endpointOverride(URI.create(properties.getEndpoint()));
		}
		this.client = builder.build();
	}

	@Override
	public String store(String category, String originalName, String contentType, byte[] content) {
		String extension = StringUtils.getFilenameExtension(originalName);
		String key = category.replaceAll("[^a-zA-Z0-9_-]", "-") + "/" + UUID.randomUUID()
				+ (StringUtils.hasText(extension) ? "." + extension.toLowerCase() : "");
		client.putObject(PutObjectRequest.builder()
				.bucket(properties.getBucket())
				.key(key)
				.contentType(contentType)
				.metadata(java.util.Map.of("original-name", sanitiseMetadata(originalName)))
				.build(), RequestBody.fromBytes(content));
		return key;
	}

	@Override
	public Optional<StoredMedia> load(String assetKey) {
		try {
			var response = client.getObjectAsBytes(GetObjectRequest.builder()
					.bucket(properties.getBucket()).key(assetKey).build());
			String originalName = response.response().metadata().getOrDefault("original-name", assetKey);
			return Optional.of(new StoredMedia(assetKey, originalName, response.response().contentType(), response.asByteArray()));
		}
		catch (NoSuchKeyException exception) {
			return Optional.empty();
		}
	}

	@Override
	public void delete(String assetKey) {
		client.deleteObject(DeleteObjectRequest.builder().bucket(properties.getBucket()).key(assetKey).build());
	}

	private String sanitiseMetadata(String value) {
		return StringUtils.hasText(value) ? value.replaceAll("[^\\x20-\\x7E]", "_") : "upload";
	}
}
