package db.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Removes legacy generated enum checks so the application enum can evolve safely. */
public class V2__expand_inquiry_status extends BaseJavaMigration {
	@Override
	public void migrate(Context context) throws Exception {
		Connection connection = context.getConnection();
		String database = connection.getMetaData().getDatabaseProductName().toLowerCase();
		List<String> constraints = new ArrayList<>();
		String query = database.contains("postgres")
				? "SELECT conname FROM pg_constraint WHERE contype='c' AND conrelid='inquiries'::regclass AND pg_get_constraintdef(oid) ILIKE '%status%'"
				: "SELECT constraint_name FROM information_schema.table_constraints WHERE lower(table_name)='inquiries' AND constraint_type='CHECK'";
		try (var statement = connection.createStatement(); ResultSet results = statement.executeQuery(query)) {
			while (results.next()) constraints.add(results.getString(1));
		}
		for (String name : constraints) {
			if (name != null && name.matches("[A-Za-z0-9_]+")) {
				try (var statement = connection.createStatement()) {
					statement.execute("ALTER TABLE inquiries DROP CONSTRAINT " + name);
				}
			}
		}
	}
}
