package co.uk.clarebrunton.ceremonies.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reviews")
public class ReviewProperties {

	private String storageDirectory = "data/reviews";

	private String photoDirectory = "uploads";

	private String adminUsername;

	private String adminPassword;

	private int maxPhotoCount = 10;

	private long maxPhotoSizeBytes = 5L * 1024L * 1024L;

	public String getStorageDirectory() {
		return storageDirectory;
	}

	public void setStorageDirectory(String storageDirectory) {
		this.storageDirectory = storageDirectory;
	}

	public String getPhotoDirectory() {
		return photoDirectory;
	}

	public void setPhotoDirectory(String photoDirectory) {
		this.photoDirectory = photoDirectory;
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public int getMaxPhotoCount() {
		return maxPhotoCount;
	}

	public void setMaxPhotoCount(int maxPhotoCount) {
		this.maxPhotoCount = maxPhotoCount;
	}

	public long getMaxPhotoSizeBytes() {
		return maxPhotoSizeBytes;
	}

	public void setMaxPhotoSizeBytes(long maxPhotoSizeBytes) {
		this.maxPhotoSizeBytes = maxPhotoSizeBytes;
	}

}
