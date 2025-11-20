package backend.databaseproject.domain.route.dto.response;

import backend.databaseproject.domain.route.entity.RoutePosition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 드론 위치 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "드론 현재 위치 정보")
public class DronePositionResponse {

    @Schema(description = "경로 ID", example = "1")
    private Long routeId;

    @Schema(description = "위도", example = "37.123456")
    private BigDecimal lat;

    @Schema(description = "경도", example = "127.123456")
    private BigDecimal lng;

    @Schema(description = "속도 (m/s)", example = "8.33")
    private BigDecimal speedMps;

    @Schema(description = "배터리 잔량 (%)", example = "85.50")
    private BigDecimal batteryPct;

    @Schema(description = "기록 시각", example = "2024-01-15T14:30:00")
    private LocalDateTime ts;

    /**
     * Entity를 DTO로 변환
     */
    public static DronePositionResponse from(RoutePosition routePosition) {
        if (routePosition == null) {
            return null;
        }

        return DronePositionResponse.builder()
                .routeId(routePosition.getRoute().getRouteId())
                .lat(routePosition.getLat())
                .lng(routePosition.getLng())
                .speedMps(routePosition.getSpeedMps())
                .batteryPct(routePosition.getBatteryPct())
                .ts(routePosition.getTs())
                .build();
    }
}
