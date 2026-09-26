package co.uk.clarebrunton.ceremonies.model;

import java.util.Map;

public record AnalyticsBreakdown(Map<String, Long> services, Map<String, Long> packages,
		Map<String, Long> sources, long enquiryOpenToSubmitRate, long formStartToSubmitRate) { }
