package backend.databaseproject.domain.route.entity;

import backend.databaseproject.domain.drone.entity.Drone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 비행 로그 엔티티
 */
@Entity
@Table(name = "flight_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id", nullable = false)
    private Drone drone;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal distance;

    @Column(name = "battery_used", nullable = false)
    private Integer batteryUsed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FlightResult result;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Builder
    public FlightLog(Route route, Drone drone, LocalDateTime startTime, LocalDateTime endTime,
                     BigDecimal distance, Integer batteryUsed, FlightResult result, String note) {
        this.route = route;
        this.drone = drone;
        this.startTime = startTime;
        this.endTime = endTime;
        this.distance = distance;
        this.batteryUsed = batteryUsed;
        this.result = result;
        this.note = note;
    }

    /**
     * 경로 설정 (양방향 관계용)
     */
    protected void setRoute(Route route) {
        this.route = route;
    }
}
