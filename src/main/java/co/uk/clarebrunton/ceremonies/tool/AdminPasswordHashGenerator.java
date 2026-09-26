package co.uk.clarebrunton.ceremonies.tool;

import java.io.Console;
import java.util.Arrays;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Local command-line helper. It never starts the web application or writes the password to disk. */
public final class AdminPasswordHashGenerator {

	private AdminPasswordHashGenerator() {
	}

	public static void main(String[] args) {
		Console console = System.console();
		if (console == null) {
			System.err.println("An interactive terminal is required so the password is not exposed in command history.");
			System.exit(2);
		}

		char[] first = console.readPassword("New admin password: ");
		char[] second = console.readPassword("Confirm password: ");
		try {
			if (first.length < 14) {
				throw new IllegalArgumentException("Use at least 14 characters.");
			}
			if (!Arrays.equals(first, second)) {
				throw new IllegalArgumentException("Passwords did not match.");
			}
			String hash = new BCryptPasswordEncoder(12).encode(new String(first));
			console.writer().println("Set ADMIN_PASSWORD_HASH to:");
			console.writer().println(hash);
		} finally {
			Arrays.fill(first, '\0');
			Arrays.fill(second, '\0');
		}
	}
}
