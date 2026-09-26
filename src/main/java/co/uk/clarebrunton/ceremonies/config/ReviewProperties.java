package co.uk.clarebrunton.ceremonies.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reviews")
public class ReviewProperties {

	private int maxPhotoCount = 10;

	private long maxPhotoSizeBytes = 5L * 1024L * 1024L;

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
