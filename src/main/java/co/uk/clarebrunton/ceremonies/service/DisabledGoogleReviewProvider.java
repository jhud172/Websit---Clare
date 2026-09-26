package co.uk.clarebrunton.ceremonies.service;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class DisabledGoogleReviewProvider implements GoogleReviewProvider {
	@Override
	public List<ExternalReview> fetchApprovedSourceReviews() { return List.of(); }
}
