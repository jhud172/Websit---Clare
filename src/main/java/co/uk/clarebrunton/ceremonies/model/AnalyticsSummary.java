package co.uk.clarebrunton.ceremonies.model;

import java.util.List;

public class AnalyticsSummary {

	private final long todayVisits;

	private final long weekVisits;

	private final long previousWeekVisits;

	private final long monthVisits;

	private final long allTimeVisits;

	private final long averageDailyVisits;

	private final String busiestDayLabel;

	private final long busiestDayVisits;

	private final List<DailyVisit> lastSevenDays;

	public AnalyticsSummary(long todayVisits,
			long weekVisits,
			long previousWeekVisits,
			long monthVisits,
			long allTimeVisits,
			long averageDailyVisits,
			String busiestDayLabel,
			long busiestDayVisits,
			List<DailyVisit> lastSevenDays) {
		this.todayVisits = todayVisits;
		this.weekVisits = weekVisits;
		this.previousWeekVisits = previousWeekVisits;
		this.monthVisits = monthVisits;
		this.allTimeVisits = allTimeVisits;
		this.averageDailyVisits = averageDailyVisits;
		this.busiestDayLabel = busiestDayLabel;
		this.busiestDayVisits = busiestDayVisits;
		this.lastSevenDays = List.copyOf(lastSevenDays);
	}

	public long getTodayVisits() {
		return todayVisits;
	}

	public long getWeekVisits() {
		return weekVisits;
	}

	public long getPreviousWeekVisits() {
		return previousWeekVisits;
	}

	public long getMonthVisits() {
		return monthVisits;
	}

	public long getAllTimeVisits() {
		return allTimeVisits;
	}

	public long getAverageDailyVisits() {
		return averageDailyVisits;
	}

	public String getBusiestDayLabel() {
		return busiestDayLabel;
	}

	public long getBusiestDayVisits() {
		return busiestDayVisits;
	}

	public List<DailyVisit> getLastSevenDays() {
		return lastSevenDays;
	}

	public long getWeekDelta() {
		return weekVisits - previousWeekVisits;
	}

	public String getWeekDeltaLabel() {
		long delta = getWeekDelta();
		if (delta > 0) {
			return "+" + delta + " vs previous 7 days";
		}
		if (delta < 0) {
			return delta + " vs previous 7 days";
		}
		return "Level with previous 7 days";
	}

	public boolean isWeekUp() {
		return getWeekDelta() > 0;
	}

	public boolean isWeekDown() {
		return getWeekDelta() < 0;
	}

	public record DailyVisit(String dayLabel, String dateLabel, long visits, int percentage) {
	}

}
