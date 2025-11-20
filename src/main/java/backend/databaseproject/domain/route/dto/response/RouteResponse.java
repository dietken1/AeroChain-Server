package backend.databaseproject.domain.route.dto.response;

import backend.databaseproject.domain.route.entity.Route;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 배송 경로 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "배송 경로 정보")
public class RouteResponse {

    @Schema(description = "경로 ID", example = "1")
    private Long routeId;

    @Schema(description = "드론 ID", example = "1")
    private Long droneId;

    @Schema(description = "드론 모델", example = "DJI Phantom 4")
    private String droneModel;

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장명", example = "스타벅스 강남점")
    private String storeName;

    @Schema(description = "경로 상태 (PLANNED, LAUNCHED, COMPLETED, ABORTED)", example = "PLANNED")
    private String status;

    @Schema(description = "계획 출발 시각", example = "2024-01-15T14:00:00")
    private LocalDateTime plannedStartAt;

    @Schema(description = "계획 도착 시각", example = "2024-01-15T15:00:00")
    private LocalDateTime plannedEndAt;

    @Schema(description = "실제 출발 시각", example = "2024-01-15T14:05:00")
    private LocalDateTime actualStartAt;

    @Schema(description = "실제 완료 시각", example = "2024-01-15T15:00:00")
    private LocalDateTime actualEndAt;

    @Schema(description = "계획 총 거리 (km)", example = "12.50")
    private BigDecimal plannedTotalDistanceKm;

    @Schema(description = "계획 총 무게 (kg)", example = "3.500")
    private BigDecimal plannedTotalPayloadKg;

    @Schema(description = "사용된 휴리스틱", example = "Nearest Neighbor")
    private String heuristic;

    @Schema(description = "정류장 목록")
    private List<RouteStopResponse> stops;

    @Schema(description = "비고", example = "Batch processed at 2024-01-15T14:00:00")
    private String note;

    /**
     * Entity를 DTO로 변환
     */
    public static RouteResponse from(Route route) {
        if (route == null) {
            return null;
        }

        // RouteStops를 RouteStopResponse로 변환
        List<RouteStopResponse> stopResponses = route.getRouteStops().stream()
                .map(RouteStopResponse::from)
                .collect(Collectors.toList());

        return RouteResponse.builder()
                .routeId(route.getRouteId())
                .droneId(route.getDrone().getDroneId())
                .droneModel(route.getDrone().getModel())
                .storeId(route.getStore().getStoreId())
                .storeName(route.getStore().getName())
                .status(route.getStatus().name())
                .plannedStartAt(route.getPlannedStartAt())
                .plannedEndAt(route.getPlannedEndAt())
                .actualStartAt(route.getActualStartAt())
                .actualEndAt(route.getActualEndAt())
                .plannedTotalDistanceKm(route.getPlannedTotalDistanceKm())
                .plannedTotalPayloadKg(route.getPlannedTotalPayloadKg())
                .heuristic(route.getHeuristic())
                .stops(stopResponses)
                .note(route.getNote())
                .build();
    }
}
