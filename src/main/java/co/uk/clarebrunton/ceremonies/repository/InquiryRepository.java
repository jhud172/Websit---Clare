package co.uk.clarebrunton.ceremonies.repository;

import java.util.List;
import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import co.uk.clarebrunton.ceremonies.model.InquiryRecord;
import co.uk.clarebrunton.ceremonies.model.InquiryStatus;

public interface InquiryRepository extends JpaRepository<InquiryRecord, String> {
	List<InquiryRecord> findAllByOrderBySubmittedAtDesc();
	long countByStatus(InquiryStatus status);
	long countBySubmittedAtBefore(OffsetDateTime cutoff);
}
