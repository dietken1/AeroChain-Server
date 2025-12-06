package backend.databaseproject.domain.route.service;

import backend.databaseproject.domain.order.entity.Order;
import backend.databaseproject.domain.order.repository.OrderRepository;
import backend.databaseproject.domain.route.entity.RouteStop;
import backend.databaseproject.domain.route.entity.RouteStopOrder;
import backend.databaseproject.domain.route.entity.StopType;
import backend.databaseproject.domain.route.repository.RouteStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RouteStop 처리 서비스 (별도 트랜잭션)
 * DroneSimulatorService에서 각 경유지 도착 처리를 즉시 커밋하기 위한 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteStopProcessingService {

    private final RouteStopRepository routeStopRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PlatformTransactionManager transactionManager;

    /**
     * Stop 도착 처리 (새로운 독립 트랜잭션)
     * 각 경유지마다 즉시 DB에 커밋하여 실시간으로 상태가 반영되도록 함
     */
    public void processStopArrival(Long stopId) {
        // 새로운 트랜잭션 정의
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            log.info("Stop 처리 시작 - StopId: {}", stopId);

            // 새 트랜잭션에서 RouteStop 조회
            RouteStop currentStop = routeStopRepository.findByIdWithOrders(stopId)
                    .orElseThrow(() -> new IllegalArgumentException("RouteStop not found: " + stopId));

            log.info("RouteStop 조회 완료 - StopId: {}, Type: {}", currentStop.getStopId(), currentStop.getStopType());

            // Stop 도착 처리
            currentStop.arrive();
            routeStopRepository.saveAndFlush(currentStop);
            log.info("Stop 도착 완료 - StopId: {}, Type: {}", currentStop.getStopId(), currentStop.getStopType());

            // DROP 타입은 잠시 대기 (배송 시뮬레이션)
            if (currentStop.getStopType() == StopType.DROP) {
                Thread.sleep(3000); // 3초 대기
                currentStop.depart();
                routeStopRepository.saveAndFlush(currentStop);

                // 이 정류장과 연결된 주문들을 완료 처리
                List<RouteStopOrder> routeStopOrders = currentStop.getRouteStopOrders();
                for (RouteStopOrder routeStopOrder : routeStopOrders) {
                    Order order = routeStopOrder.getOrder();
                    order.completeDelivery();
                    orderRepository.saveAndFlush(order);

                    log.info("주문 완료 처리 - OrderId: {}, User: {}",
                            order.getOrderId(), order.getUser().getName());

                    // WebSocket으로 배송 완료 알림 전송
                    Map<String, Object> completionData = new HashMap<>();
                    completionData.put("orderId", order.getOrderId());
                    completionData.put("status", "FULFILLED");
                    completionData.put("message", "배송이 완료되었습니다!");
                    completionData.put("completedAt", LocalDateTime.now());
                    messagingTemplate.convertAndSend(
                            "/topic/order/" + order.getOrderId(),
                            completionData
                    );

                    log.info("배송 완료 알림 전송 - OrderId: {}", order.getOrderId());
                }
            }

            // 트랜잭션 커밋
            transactionManager.commit(status);
            log.info("Stop 처리 완료 및 커밋됨 - StopId: {}", stopId);

        } catch (Exception e) {
            // 트랜잭션 롤백
            transactionManager.rollback(status);
            log.error("Stop 처리 중 오류 발생 - StopId: {}", stopId, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Stop 처리 실패", e);
        }
    }
}
