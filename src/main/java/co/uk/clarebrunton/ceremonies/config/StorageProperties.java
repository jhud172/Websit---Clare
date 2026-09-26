package co.uk.clarebrunton.ceremonies.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

	private String provider = "database";
	private String bucket;
	private String region = "eu-west-2";
	private String endpoint;
	private String accessKey;
	private String secretKey;

	public String getProvider() { return provider; }
	public void setProvider(String provider) { this.provider = provider; }
	public String getBucket() { return bucket; }
	public void setBucket(String bucket) { this.bucket = bucket; }
	public String getRegion() { return region; }
	public void setRegion(String region) { this.region = region; }
	public String getEndpoint() { return endpoint; }
	public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
	public String getAccessKey() { return accessKey; }
	public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
	public String getSecretKey() { return secretKey; }
	public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
}
