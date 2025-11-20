package backend.databaseproject.domain.route.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 배송 경로 드론 위치 추적 엔티티
 */
@Entity
@Table(name = "route_position")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pos_id")
    private Long posId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_from_id")
    private RouteStop stopFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_to_id")
    private RouteStop stopTo;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal lng;

    @Column(name = "speed_mps", precision = 6, scale = 2)
    private BigDecimal speedMps;

    @Column(name = "battery_pct", precision = 5, scale = 2)
    private BigDecimal batteryPct;

    @Column(nullable = false)
    private LocalDateTime ts;

    @PrePersist
    protected void onCreate() {
        if (ts == null) {
            ts = LocalDateTime.now();
        }
    }

    @Builder
    public RoutePosition(Route route, RouteStop stopFrom, RouteStop stopTo,
                         BigDecimal lat, BigDecimal lng, BigDecimal speedMps,
                         BigDecimal batteryPct, LocalDateTime ts) {
        this.route = route;
        this.stopFrom = stopFrom;
        this.stopTo = stopTo;
        this.lat = lat;
        this.lng = lng;
        this.speedMps = speedMps;
        this.batteryPct = batteryPct;
        this.ts = ts;
    }

    /**
     * 경로 설정 (양방향 관계용)
     */
    protected void setRoute(Route route) {
        this.route = route;
    }
}
