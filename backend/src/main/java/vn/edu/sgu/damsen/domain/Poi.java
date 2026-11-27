package vn.edu.sgu.damsen.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pois")
public class Poi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "zone_id") private Long zoneId;
    private String code;
    @Column(precision = 9, scale = 6) private BigDecimal latitude;
    @Column(precision = 9, scale = 6) private BigDecimal longitude;
    @Column(name = "radius_m") private int radiusM = 30;
    private String status = "ACTIVE";

    public boolean isActive() { return "ACTIVE".equals(status); }

    public Long getId() { return id; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public int getRadiusM() { return radiusM; }
    public void setRadiusM(int radiusM) { this.radiusM = radiusM; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
