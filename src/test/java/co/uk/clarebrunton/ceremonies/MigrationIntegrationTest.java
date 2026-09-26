package co.uk.clarebrunton.ceremonies;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class MigrationIntegrationTest {

	@Autowired Flyway flyway;
	@Autowired JdbcTemplate jdbc;

	@Test
	void flywayCreatesTheCompleteSchemaBeforeJpaValidation() {
		assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("3");
		List<String> tables = jdbc.queryForList("""
				SELECT table_name
				FROM information_schema.tables
				WHERE table_schema = 'public'
				""", String.class);
		assertThat(tables).contains(
				"inquiries", "inquiry_attachments", "reviews", "review_photos",
				"stored_assets", "analytics_daily_visits", "analytics_funnel_events",
				"flyway_schema_history");
	}

	@Test
	void importantConstraintsAndIndexesExist() {
		Integer statusNullable = jdbc.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.columns
				WHERE table_schema = 'public'
				  AND table_name = 'inquiries'
				  AND column_name = 'status'
				  AND is_nullable = 'NO'
				""", Integer.class);
		assertThat(statusNullable).isEqualTo(1);

		Integer optionalReviewDate = jdbc.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.columns
				WHERE table_schema = 'public'
				  AND table_name = 'reviews'
				  AND column_name = 'event_date'
				  AND is_nullable = 'YES'
				""", Integer.class);
		assertThat(optionalReviewDate).isEqualTo(1);

		List<String> indexes = jdbc.queryForList("""
				SELECT index_name
				FROM information_schema.indexes
				WHERE table_schema = 'public'
				""", String.class);
		assertThat(indexes).contains("idx_inquiries_status_submitted", "idx_funnel_event_type_recorded");
	}
}
