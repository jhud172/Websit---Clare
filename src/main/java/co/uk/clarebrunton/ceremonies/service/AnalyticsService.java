package co.uk.clarebrunton.ceremonies.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.uk.clarebrunton.ceremonies.config.AnalyticsProperties;
import co.uk.clarebrunton.ceremonies.model.AnalyticsSummary;

@Service
public class AnalyticsService {

	private static final String DATA_FILE_NAME = "visits-by-day.json";

	private static final TypeReference<Map<String, Long>> VISIT_COUNTS_TYPE = new TypeReference<>() {
	};

	private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("EEE");

	private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("d MMM");

	private final AnalyticsProperties analyticsProperties;

	private final ObjectMapper objectMapper;

	public AnalyticsService(AnalyticsProperties analyticsProperties) {
		this.analyticsProperties = analyticsProperties;
		this.objectMapper = new ObjectMapper();
	}

	public synchronized void recordVisit() {
		Map<String, Long> visitsByDay = loadAll();
		String todayKey = LocalDate.now().toString();
		visitsByDay.put(todayKey, visitsByDay.getOrDefault(todayKey, 0L) + 1);
		saveAll(visitsByDay);
	}

	public synchronized AnalyticsSummary getSummary() {
		Map<LocalDate, Long> visitsByDate = loadAllByDate();
		LocalDate today = LocalDate.now();
		List<AnalyticsSummary.DailyVisit> lastSevenDays = buildLastSevenDays(visitsByDate, today);
		long busiestVisits = lastSevenDays.stream()
				.mapToLong(AnalyticsSummary.DailyVisit::visits)
				.max()
				.orElse(0L);
		String busiestDay = lastSevenDays.stream()
				.max(Comparator.comparingLong(AnalyticsSummary.DailyVisit::visits))
				.map(AnalyticsSummary.DailyVisit::dayLabel)
				.orElse("No visits yet");
		long weekVisits = sumSince(visitsByDate, today.minusDays(6));
		long previousWeekVisits = sumBetween(visitsByDate, today.minusDays(13), today.minusDays(7));

		return new AnalyticsSummary(
				sumSince(visitsByDate, today),
				weekVisits,
				previousWeekVisits,
				sumSince(visitsByDate, today.minusDays(29)),
				visitsByDate.values().stream().mapToLong(Long::longValue).sum(),
				Math.round(weekVisits / 7.0),
				busiestVisits > 0 ? busiestDay : "No visits yet",
				busiestVisits,
				lastSevenDays
		);
	}

	private long sumSince(Map<LocalDate, Long> visitsByDate, LocalDate startDate) {
		return visitsByDate.entrySet().stream()
				.filter(entry -> !entry.getKey().isBefore(startDate))
				.mapToLong(Map.Entry::getValue)
				.sum();
	}

	private long sumBetween(Map<LocalDate, Long> visitsByDate, LocalDate startDate, LocalDate endDate) {
		return visitsByDate.entrySet().stream()
				.filter(entry -> !entry.getKey().isBefore(startDate) && !entry.getKey().isAfter(endDate))
				.mapToLong(Map.Entry::getValue)
				.sum();
	}

	private List<AnalyticsSummary.DailyVisit> buildLastSevenDays(Map<LocalDate, Long> visitsByDate, LocalDate today) {
		List<LocalDate> days = IntStream.rangeClosed(0, 6)
				.mapToObj(offset -> today.minusDays(6L - offset))
				.toList();
		long maxVisits = days.stream()
				.mapToLong(day -> visitsByDate.getOrDefault(day, 0L))
				.max()
				.orElse(0L);

		return days.stream()
				.map(day -> {
					long visits = visitsByDate.getOrDefault(day, 0L);
					int percentage = maxVisits > 0 ? Math.max(8, (int) Math.round((visits * 100.0) / maxVisits)) : 0;
					return new AnalyticsSummary.DailyVisit(
							DAY_LABEL_FORMATTER.format(day),
							DATE_LABEL_FORMATTER.format(day),
							visits,
							percentage
					);
				})
				.toList();
	}

	private Map<LocalDate, Long> loadAllByDate() {
		Map<LocalDate, Long> visitsByDate = new TreeMap<>();

		for (Map.Entry<String, Long> entry : loadAll().entrySet()) {
			try {
				visitsByDate.put(LocalDate.parse(entry.getKey()), entry.getValue());
			}
			catch (RuntimeException ignored) {
				// Ignore malformed legacy rows rather than breaking the admin dashboard.
			}
		}

		return visitsByDate;
	}

	private Map<String, Long> loadAll() {
		Path file = getDataFilePath();

		if (!Files.exists(file)) {
			return new LinkedHashMap<>();
		}

		try {
			return objectMapper.readValue(file.toFile(), VISIT_COUNTS_TYPE);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Unable to read analytics data.", exception);
		}
	}

	private void saveAll(Map<String, Long> visitsByDay) {
		Path file = getDataFilePath();

		try {
			Files.createDirectories(file.getParent());
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), visitsByDay);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Unable to save analytics data.", exception);
		}
	}

	private Path getDataFilePath() {
		return Path.of(analyticsProperties.getStorageDirectory()).resolve(DATA_FILE_NAME).toAbsolutePath().normalize();
	}

}
