package co.uk.clarebrunton.ceremonies.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.uk.clarebrunton.ceremonies.model.ReviewEntry;
import co.uk.clarebrunton.ceremonies.model.ReviewStatus;

public interface ReviewRepository extends JpaRepository<ReviewEntry, String> {
	List<ReviewEntry> findByStatusOrderBySubmittedAtDesc(ReviewStatus status);
	List<ReviewEntry> findAllByOrderBySubmittedAtDesc();
	long countBySubmittedAtBefore(java.time.OffsetDateTime cutoff);
}
