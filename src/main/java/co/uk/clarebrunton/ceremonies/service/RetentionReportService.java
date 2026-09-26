package co.uk.clarebrunton.ceremonies.service;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.uk.clarebrunton.ceremonies.config.RetentionProperties;
import co.uk.clarebrunton.ceremonies.repository.FunnelEventRepository;
import co.uk.clarebrunton.ceremonies.repository.InquiryRepository;
import co.uk.clarebrunton.ceremonies.repository.ReviewRepository;
import co.uk.clarebrunton.ceremonies.repository.StoredAssetRepository;

@Service
public class RetentionReportService {
	private final RetentionProperties properties;
	private final InquiryRepository inquiries;
	private final ReviewRepository reviews;
	private final FunnelEventRepository analytics;
	private final StoredAssetRepository assets;
	public RetentionReportService(RetentionProperties properties, InquiryRepository inquiries, ReviewRepository reviews,
			FunnelEventRepository analytics, StoredAssetRepository assets) {
		this.properties = properties; this.inquiries = inquiries; this.reviews = reviews; this.analytics = analytics; this.assets = assets;
	}
	@Transactional(readOnly = true)
	public Report report() {
		OffsetDateTime now = OffsetDateTime.now();
		return new Report(false,
				inquiries.countBySubmittedAtBefore(now.minus(properties.getInquiries())),
				reviews.countBySubmittedAtBefore(now.minus(properties.getReviews())),
				analytics.countByRecordedAtBefore(now.minus(properties.getAnalytics())),
				assets.countByCreatedAtBefore(now.minus(properties.getAssets())));
	}
	public record Report(boolean automaticDeletionActive, long oldInquiries, long oldReviews, long oldAnalyticsEvents, long oldAssets) { }
}
