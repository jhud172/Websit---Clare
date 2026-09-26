package co.uk.clarebrunton.ceremonies.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analytics_funnel_events")
public class FunnelEvent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String eventType;
	private String pagePath;
	private String serviceType;
	private String packageName;
	@Column(nullable = false)
	private OffsetDateTime recordedAt;
	public Long getId() { return id; }
	public String getEventType() { return eventType; }
	public void setEventType(String eventType) { this.eventType = eventType; }
	public String getPagePath() { return pagePath; }
	public void setPagePath(String pagePath) { this.pagePath = pagePath; }
	public String getServiceType() { return serviceType; }
	public void setServiceType(String serviceType) { this.serviceType = serviceType; }
	public String getPackageName() { return packageName; }
	public void setPackageName(String packageName) { this.packageName = packageName; }
	public OffsetDateTime getRecordedAt() { return recordedAt; }
	public void setRecordedAt(OffsetDateTime recordedAt) { this.recordedAt = recordedAt; }
}
