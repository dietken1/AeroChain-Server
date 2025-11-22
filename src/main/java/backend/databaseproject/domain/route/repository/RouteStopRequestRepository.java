package backend.databaseproject.domain.route.repository;

import backend.databaseproject.domain.route.entity.RouteStopRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 경로 정류장-배송요청 매핑 Repository
 */
@Repository
public interface RouteStopRequestRepository extends JpaRepository<RouteStopRequest, Long> {

    /**
     * 배송 요청 ID로 경로 ID 조회
     * 주문이 배송 경로에 할당된 경우 해당 경로 ID를 반환
     *
     * @param requestId 배송 요청 ID
     * @return 경로 ID (Optional)
     */
    @Query("SELECT rsr.routeStop.route.routeId FROM RouteStopRequest rsr " +
           "WHERE rsr.deliveryRequest.requestId = :requestId")
    Optional<Long> findRouteIdByRequestId(@Param("requestId") Long requestId);
}
