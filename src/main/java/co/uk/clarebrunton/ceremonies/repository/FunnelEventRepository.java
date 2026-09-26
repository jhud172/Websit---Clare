package co.uk.clarebrunton.ceremonies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import co.uk.clarebrunton.ceremonies.model.FunnelEvent;
public interface FunnelEventRepository extends JpaRepository<FunnelEvent, Long> {
	long countByEventType(String eventType);
	long countByRecordedAtBefore(java.time.OffsetDateTime cutoff);
}
