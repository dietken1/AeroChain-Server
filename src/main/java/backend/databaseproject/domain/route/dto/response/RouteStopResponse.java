package backend.databaseproject.domain.route.dto.response;

import backend.databaseproject.domain.route.entity.RouteStop;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경로 정류장 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "경로 정류장 정보")
public class RouteStopResponse {

    @Schema(description = "정류장 ID", example = "1")
    private Long stopId;

    @Schema(description = "정류장 순서", example = "1")
    private Integer stopSeq;

    @Schema(description = "정류장 타입 (PICKUP, DROP, RETURN)", example = "PICKUP")
    private String type;

    @Schema(description = "정류장 이름 (매장명 또는 고객명)", example = "스타벅스 강남점")
    private String name;

    @Schema(description = "위도", example = "37.123456")
    private BigDecimal lat;

    @Schema(description = "경도", example = "127.123456")
    private BigDecimal lng;

    @Schema(description = "정류장 상태 (PENDING, ARRIVED, DEPARTED, SKIPPED)", example = "PENDING")
    private String status;

    @Schema(description = "계획 도착 시각", example = "2024-01-15T14:25:00")
    private LocalDateTime plannedArrivalAt;

    @Schema(description = "계획 출발 시각", example = "2024-01-15T14:27:00")
    private LocalDateTime plannedDepartureAt;

    @Schema(description = "실제 도착 시각", example = "2024-01-15T14:30:00")
    private LocalDateTime actualArrivalAt;

    @Schema(description = "실제 출발 시각", example = "2024-01-15T14:35:00")
    private LocalDateTime actualDepartureAt;

    @Schema(description = "적재량 변화 (kg)", example = "-1.500")
    private BigDecimal payloadDeltaKg;

    /**
     * Entity를 DTO로 변환
     */
    public static RouteStopResponse from(RouteStop routeStop) {
        if (routeStop == null) {
            return null;
        }

        return RouteStopResponse.builder()
                .stopId(routeStop.getStopId())
                .stopSeq(routeStop.getStopSequence())
                .type(routeStop.getStopType().name())
                .name(routeStop.getName())
                .lat(routeStop.getLat())
                .lng(routeStop.getLng())
                .status(routeStop.getStatus().name())
                .plannedArrivalAt(routeStop.getPlannedArrivalAt())
                .plannedDepartureAt(routeStop.getPlannedDepartureAt())
                .actualArrivalAt(routeStop.getActualArrivalAt())
                .actualDepartureAt(routeStop.getActualDepartureAt())
                .payloadDeltaKg(routeStop.getPayloadDeltaKg())
                .build();
    }
}
