package backend.databaseproject.domain.route.controller;

import backend.databaseproject.domain.route.dto.request.StartDeliveryRequest;
import backend.databaseproject.domain.route.dto.response.DronePositionResponse;
import backend.databaseproject.domain.route.dto.response.RouteResponse;
import backend.databaseproject.domain.route.service.DeliveryBatchService;
import backend.databaseproject.domain.route.service.RouteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 배송 경로 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Route", description = "배송 경로 관련 API")
@Slf4j
public class RouteController {

    private final RouteService routeService;
    private final DeliveryBatchService deliveryBatchService;

    /**
     * 경로 상세 조회
     * 특정 배송 경로의 상세 정보를 조회합니다.
     *
     * @param routeId 경로 ID
     * @return 경로 상세 정보
     */
    @GetMapping("/{routeId}")
    @Operation(
            summary = "경로 조회",
            description = "특정 배송 경로의 상세 정보를 조회합니다. 경로에 포함된 모든 정류장 정보를 함께 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공"
                            
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 배송 경로"
                    )
            }
    )
    public RouteResponse getRoute(
            @Parameter(name = "routeId", description = "경로 ID", required = true, example = "1")
            @PathVariable Long routeId
    ) {
        log.info("API 호출: GET /api/routes/{}", routeId);
        RouteResponse route = routeService.getRoute(routeId);
        return route;
    }

    /**
     * 드론 현재 위치 조회
     * 특정 경로의 드론 현재 위치를 조회합니다.
     *
     * @param routeId 경로 ID
     * @return 드론 현재 위치 정보
     */
    @GetMapping("/{routeId}/current-position")
    @Operation(
            summary = "드론 현재 위치 조회",
            description = "특정 배송 경로의 드론 현재 위치를 실시간으로 조회합니다. " +
                         "위도, 경도, 고도, 속도, 배터리 잔량 등의 정보를 포함합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공"
                            
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 배송 경로 또는 위치 정보 없음"
                    )
            }
    )
    public DronePositionResponse getCurrentPosition(
            @Parameter(name = "routeId", description = "경로 ID", required = true, example = "1")
            @PathVariable Long routeId
    ) {
        log.info("API 호출: GET /api/routes/{}/current-position", routeId);
        DronePositionResponse position = routeService.getCurrentPosition(routeId);
        return position;
    }

    /**
     * 진행 중인 배송 목록 조회
     * 현재 진행 중인 모든 배송 경로 목록을 조회합니다.
     *
     * @return 진행 중인 배송 경로 목록
     */
    @GetMapping("/active")
    @Operation(
            summary = "진행 중인 배송 목록 조회",
            description = "현재 진행 중인 모든 배송 경로 목록을 조회합니다. " +
                         "LAUNCHED 또는 IN_PROGRESS 상태인 경로들을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공"
                            
                    )
            }
    )
    public List<RouteResponse> getActiveRoutes() {
        log.info("API 호출: GET /api/routes/active");
        List<RouteResponse> activeRoutes = routeService.getActiveRoutes();
        return activeRoutes;
    }

    /**
     * 배송 시작
     * 점주가 선택한 주문들을 배송 시작합니다.
     *
     * @param request 배송할 주문 ID 리스트 (최대 3개)
     * @return 성공 메시지
     */
    @PostMapping("/start-delivery")
    @Operation(
            summary = "배송 시작",
            description = "점주가 선택한 주문들(최대 3개)을 배송 시작합니다. " +
                         "모든 주문은 같은 매장이어야 하며, CREATED 상태여야 합니다. " +
                         "드론의 최대 무게와 배터리를 고려하여 검증 후 배송을 시작합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "배송 시작 성공"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (주문 없음, 상태 오류, 매장 불일치, 적재량 초과, 배터리 부족 등)"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "주문 또는 드론을 찾을 수 없음"
                    )
            }
    )
    public ResponseEntity<String> startDelivery(
            @RequestBody @Valid StartDeliveryRequest request
    ) {
        log.info("API 호출: POST /api/routes/start-delivery");
        log.info("요청 주문 ID: {}", request.getOrderIds());

        try {
            deliveryBatchService.processSelectedOrders(request.getOrderIds());
            return ResponseEntity.ok("배송이 성공적으로 시작되었습니다.");
        } catch (Exception e) {
            log.error("배송 시작 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 배송 배치 처리 (전체 자동)
     * 현재까지 쌓여있는 모든 주문들을 자동으로 배송 시작합니다.
     * 스케줄러나 관리자용 API입니다.
     *
     * @return 성공 메시지
     */
    @PostMapping("/batch-delivery")
    @Operation(
            summary = "배송 배치 처리 (전체 자동)",
            description = "현재까지 대기 중인 모든 주문을 자동으로 수집하여 배송을 시작합니다. " +
                         "드론의 최대 무게와 배터리를 고려하여 먼저 온 주문 순으로 최적 경로를 탐색합니다. " +
                         "스케줄러 또는 관리자용 API입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "배송 시작 성공"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "사용 가능한 드론이 없거나 대기 중인 주문이 없음"
                    )
            }
    )
    public ResponseEntity<String> batchDelivery() {
        log.info("API 호출: POST /api/routes/batch-delivery");

        try {
            deliveryBatchService.processBatch();
            return ResponseEntity.ok("배송 배치 처리가 완료되었습니다.");
        } catch (Exception e) {
            log.error("배송 배치 처리 중 오류 발생", e);
            throw e;
        }
    }
}
