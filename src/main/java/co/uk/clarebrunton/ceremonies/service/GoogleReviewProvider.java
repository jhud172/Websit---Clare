package co.uk.clarebrunton.ceremonies.service;

import java.util.List;

/** Extension point for a future official Google Business Profile API client. Never scrape review pages. */
public interface GoogleReviewProvider {
	List<ExternalReview> fetchApprovedSourceReviews();
	record ExternalReview(String providerId, String reviewerName, int rating, String text) { }
}
