package co.uk.clarebrunton.ceremonies.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.uk.clarebrunton.ceremonies.model.StoredAsset;

public interface StoredAssetRepository extends JpaRepository<StoredAsset, String> {
	long countByCreatedAtBefore(java.time.OffsetDateTime cutoff);
}
