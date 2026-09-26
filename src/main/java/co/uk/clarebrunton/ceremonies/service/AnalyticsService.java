package co.uk.clarebrunton.ceremonies.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.uk.clarebrunton.ceremonies.model.AnalyticsSummary;
import co.uk.clarebrunton.ceremonies.model.AnalyticsBreakdown;
import co.uk.clarebrunton.ceremonies.model.DailyVisit;
import co.uk.clarebrunton.ceremonies.model.FunnelEvent;
import co.uk.clarebrunton.ceremonies.repository.DailyVisitRepository;
import co.uk.clarebrunton.ceremonies.repository.FunnelEventRepository;

@Service
public class AnalyticsService {

	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE");
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM");
	private final DailyVisitRepository dailyVisits;
	private final FunnelEventRepository funnelEvents;

	public AnalyticsService(DailyVisitRepository dailyVisits, FunnelEventRepository funnelEvents) {
		this.dailyVisits = dailyVisits;
		this.funnelEvents = funnelEvents;
	}

	@Transactional
	public void recordVisit() {
		LocalDate today = LocalDate.now();
		DailyVisit visit = dailyVisits.findById(today).orElseGet(() -> {
			DailyVisit created = new DailyVisit();
			created.setVisitDate(today);
			return created;
		});
		visit.setVisitCount(visit.getVisitCount() + 1);
		dailyVisits.save(visit);
	}

	@Transactional
	public void recordFunnelEvent(String eventType, String pagePath, String serviceType, String packageName) {
		if (!List.of("SERVICE_VIEWED", "PACKAGE_VIEWED", "ENQUIRY_OPENED", "FORM_STARTED", "DATE_SELECTED", "SUBMISSION_ATTEMPTED", "ENQUIRY_SUBMITTED").contains(eventType)) {
			throw new IllegalArgumentException("Unsupported analytics event.");
		}
		FunnelEvent event = new FunnelEvent();
		event.setEventType(eventType);
		event.setPagePath(limit(pagePath));
		event.setServiceType(limit(serviceType));
		event.setPackageName(limit(packageName));
		event.setRecordedAt(OffsetDateTime.now());
		funnelEvents.save(event);
	}

	@Transactional(readOnly = true)
	public Map<String, Long> getFunnelSummary() {
		Map<String, Long> summary = new LinkedHashMap<>();
		for (String event : List.of("SERVICE_VIEWED", "PACKAGE_VIEWED", "ENQUIRY_OPENED", "FORM_STARTED", "DATE_SELECTED", "SUBMISSION_ATTEMPTED", "ENQUIRY_SUBMITTED")) {
			summary.put(event, funnelEvents.countByEventType(event));
		}
		return summary;
	}

	@Transactional(readOnly = true)
	public AnalyticsBreakdown getBreakdown() {
		List<FunnelEvent> events = funnelEvents.findAll();
		Map<String, Long> services = grouped(events, "service");
		Map<String, Long> packages = grouped(events, "package");
		Map<String, Long> sources = grouped(events, "source");
		long submitted = events.stream().filter(event -> "ENQUIRY_SUBMITTED".equals(event.getEventType())).count();
		long opened = events.stream().filter(event -> "ENQUIRY_OPENED".equals(event.getEventType())).count();
		long started = events.stream().filter(event -> "FORM_STARTED".equals(event.getEventType())).count();
		return new AnalyticsBreakdown(services, packages, sources, rate(submitted, opened), rate(submitted, started));
	}

	@Transactional(readOnly = true)
	public AnalyticsSummary getSummary() {
		Map<LocalDate, Long> byDate = new LinkedHashMap<>();
		dailyVisits.findAll().forEach(visit -> byDate.put(visit.getVisitDate(), visit.getVisitCount()));
		LocalDate today = LocalDate.now();
		List<AnalyticsSummary.DailyVisit> lastSeven = buildLastSevenDays(byDate, today);
		long busiest = lastSeven.stream().mapToLong(AnalyticsSummary.DailyVisit::visits).max().orElse(0);
		String busiestDay = lastSeven.stream().max(Comparator.comparingLong(AnalyticsSummary.DailyVisit::visits))
				.map(AnalyticsSummary.DailyVisit::dayLabel).orElse("No visits yet");
		long week = sumSince(byDate, today.minusDays(6));
		long previousWeek = sumBetween(byDate, today.minusDays(13), today.minusDays(7));
		return new AnalyticsSummary(sumSince(byDate, today), week, previousWeek,
				sumSince(byDate, today.minusDays(29)), byDate.values().stream().mapToLong(Long::longValue).sum(),
				Math.round(week / 7.0), busiest > 0 ? busiestDay : "No visits yet", busiest, lastSeven);
	}

	private List<AnalyticsSummary.DailyVisit> buildLastSevenDays(Map<LocalDate, Long> byDate, LocalDate today) {
		List<LocalDate> days = IntStream.rangeClosed(0, 6).mapToObj(offset -> today.minusDays(6L - offset)).toList();
		long max = days.stream().mapToLong(day -> byDate.getOrDefault(day, 0L)).max().orElse(0);
		return days.stream().map(day -> {
			long visits = byDate.getOrDefault(day, 0L);
			int percentage = max > 0 ? Math.max(8, (int) Math.round(visits * 100.0 / max)) : 0;
			return new AnalyticsSummary.DailyVisit(DAY.format(day), DATE.format(day), visits, percentage);
		}).toList();
	}

	private long sumSince(Map<LocalDate, Long> values, LocalDate start) {
		return values.entrySet().stream().filter(entry -> !entry.getKey().isBefore(start)).mapToLong(Map.Entry::getValue).sum();
	}

	private long sumBetween(Map<LocalDate, Long> values, LocalDate start, LocalDate end) {
		return values.entrySet().stream().filter(entry -> !entry.getKey().isBefore(start) && !entry.getKey().isAfter(end)).mapToLong(Map.Entry::getValue).sum();
	}

	private String limit(String value) {
		if (value == null || value.isBlank()) return null;
		String clean = value.trim();
		return clean.length() <= 120 ? clean : clean.substring(0, 120);
	}

	private Map<String, Long> grouped(List<FunnelEvent> events, String dimension) {
		Map<String, Long> values = new LinkedHashMap<>();
		for (FunnelEvent event : events) {
			String value = switch (dimension) {
				case "service" -> event.getServiceType();
				case "package" -> event.getPackageName();
				default -> event.getPagePath();
			};
			if (value != null && !value.isBlank()) values.merge(value, 1L, Long::sum);
		}
		return values.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.limit(8).collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						(left, right) -> left, LinkedHashMap::new));
	}

	private long rate(long completed, long started) { return started == 0 ? 0 : Math.min(100, Math.round(completed * 100.0 / started)); }
}
