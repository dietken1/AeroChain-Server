package backend.databaseproject.domain.route.repository;

import backend.databaseproject.domain.route.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 경로 정류장 Repository
 */
@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    /**
     * 특정 경로의 정류장 목록 조회 (순서대로)
     */
    List<RouteStop> findByRouteRouteIdOrderByStopSequence(Long routeId);

    /**
     * 특정 경로의 다음 정류장 조회 (PENDING 상태)
     */
    @Query("SELECT rs FROM RouteStop rs " +
           "WHERE rs.route.routeId = :routeId AND rs.status = 'PENDING' " +
           "ORDER BY rs.stopSequence ASC " +
           "LIMIT 1")
    RouteStop findNextPendingStop(@Param("routeId") Long routeId);

    /**
     * RouteStop을 RouteStopOrders와 함께 조회
     */
    @Query("SELECT rs FROM RouteStop rs " +
           "LEFT JOIN FETCH rs.routeStopOrders rso " +
           "LEFT JOIN FETCH rso.order o " +
           "LEFT JOIN FETCH o.user " +
           "WHERE rs.stopId = :stopId")
    Optional<RouteStop> findByIdWithOrders(@Param("stopId") Long stopId);

    /**
     * 여러 RouteStop의 RouteStopOrders를 한 번에 fetch
     */
    @Query("SELECT DISTINCT rs FROM RouteStop rs " +
           "LEFT JOIN FETCH rs.routeStopOrders rso " +
           "LEFT JOIN FETCH rso.order " +
           "WHERE rs.stopId IN :stopIds")
    List<RouteStop> findAllWithOrdersByIds(@Param("stopIds") List<Long> stopIds);
}
