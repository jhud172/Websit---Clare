package co.uk.clarebrunton.ceremonies.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analytics_daily_visits")
public class DailyVisit {
	@Id
	@Column(nullable = false)
	private LocalDate visitDate;
	@Column(nullable = false)
	private long visitCount;
	public LocalDate getVisitDate() { return visitDate; }
	public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
	public long getVisitCount() { return visitCount; }
	public void setVisitCount(long visitCount) { this.visitCount = visitCount; }
}
