package co.uk.clarebrunton.ceremonies.model;

import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stored_assets")
public class StoredAsset {

	@Id
	private String assetKey;
	@Column(nullable = false)
	private String originalName;
	@Column(nullable = false)
	private String contentType;
	@Column(nullable = false)
	private long sizeBytes;
	@Column(nullable = false)
	private OffsetDateTime createdAt;

	@JdbcTypeCode(SqlTypes.VARBINARY)
	@Column(nullable = false, columnDefinition = "bytea")
	private byte[] content;

	public String getAssetKey() { return assetKey; }
	public void setAssetKey(String assetKey) { this.assetKey = assetKey; }
	public String getOriginalName() { return originalName; }
	public void setOriginalName(String originalName) { this.originalName = originalName; }
	public String getContentType() { return contentType; }
	public void setContentType(String contentType) { this.contentType = contentType; }
	public long getSizeBytes() { return sizeBytes; }
	public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
	public byte[] getContent() { return content; }
	public void setContent(byte[] content) { this.content = content; }
}
