# WebSocket 실시간 드론 추적 가이드

## 개요

이 시스템은 두 가지 WebSocket 구독 방식을 제공합니다:

1. **점주용**: RouteId로 전체 배송 경로 추적
2. **고객용**: OrderId로 자신의 주문만 추적

## 1. 점주용 - Route 기반 추적

### 사용 시나리오
- 점주가 자신의 매장에서 출발한 드론의 전체 경로를 추적
- 한 번의 배송에 여러 고객 주문이 포함될 수 있음

### WebSocket 구독
```javascript
// Route ID로 구독
stompClient.subscribe('/topic/route/{routeId}', function(message) {
    const data = JSON.parse(message.body);
    // data.routeId, data.lat, data.lng, data.speed, data.battery
});
```

### 응답 데이터
```json
{
    "routeId": 1,
    "lat": 37.497900,
    "lng": 127.027600,
    "speed": 30.0,
    "battery": 95.5,
    "timestamp": "2025-12-05T19:00:00"
}
```

### 사용 방법
1. `/api/routes/start-delivery` API로 배송 시작
2. 응답에서 또는 `/api/stores/{storeId}/orders?status=ASSIGNED`에서 RouteId 확인
3. WebSocket으로 `/topic/route/{routeId}` 구독
4. 실시간 위치 정보 수신

### 예제 파일
`client-examples/drone-tracking.html`

---

## 2. 고객용 - Order 기반 추적

### 사용 시나리오
- 고객이 자신이 주문한 상품을 배송하는 드론만 추적
- 자신의 주문 ID만 알면 됨 (RouteId 불필요)

### WebSocket 구독

#### 2.1 드론 위치 추적
```javascript
// Order ID로 드론 위치 구독
stompClient.subscribe('/topic/order/{orderId}/position', function(message) {
    const data = JSON.parse(message.body);
    // data.orderId, data.lat, data.lng, data.speed, data.battery, data.status
});
```

#### 2.2 배송 완료 알림
```javascript
// Order ID로 배송 완료 알림 구독 (기존)
stompClient.subscribe('/topic/order/{orderId}', function(message) {
    const data = JSON.parse(message.body);
    // data.status === 'FULFILLED' -> 배송 완료
});
```

### 응답 데이터

#### 드론 위치 (`/topic/order/{orderId}/position`)
```json
{
    "orderId": 10,
    "lat": 37.497900,
    "lng": 127.027600,
    "speed": 30.0,
    "battery": 95.5,
    "timestamp": "2025-12-05T19:00:00",
    "status": "IN_TRANSIT"
}
```

#### 배송 완료 (`/topic/order/{orderId}`)
```json
{
    "orderId": 10,
    "status": "FULFILLED",
    "message": "배송이 완료되었습니다!",
    "completedAt": "2025-12-05T19:10:00"
}
```

### 사용 방법
1. `/api/orders` API로 주문 생성
2. 응답에서 `orderId` 확인
3. WebSocket으로 다음 두 가지 구독:
   - `/topic/order/{orderId}/position` - 드론 위치
   - `/topic/order/{orderId}` - 배송 완료 알림
4. 실시간 위치 정보 및 배송 완료 알림 수신

### 예제 파일
`client-examples/customer-tracking.html`

---

## 프론트엔드 구현 예제

### React/Vue/Angular 등에서 사용

```javascript
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class OrderTracker {
    constructor(orderId) {
        this.orderId = orderId;
        this.stompClient = null;
    }

    connect() {
        const socket = new SockJS('http://localhost:8080/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, () => {
            console.log('WebSocket 연결 성공');

            // 드론 위치 구독
            this.stompClient.subscribe(
                `/topic/order/${this.orderId}/position`,
                (message) => {
                    const position = JSON.parse(message.body);
                    this.onPositionUpdate(position);
                }
            );

            // 배송 완료 알림 구독
            this.stompClient.subscribe(
                `/topic/order/${this.orderId}`,
                (message) => {
                    const status = JSON.parse(message.body);
                    if (status.status === 'FULFILLED') {
                        this.onDeliveryComplete(status);
                    }
                }
            );
        });
    }

    onPositionUpdate(position) {
        console.log('드론 위치:', position.lat, position.lng);
        // 지도 마커 업데이트
        // this.updateMap(position.lat, position.lng);
    }

    onDeliveryComplete(status) {
        console.log('배송 완료!');
        // UI 업데이트
        // this.showCompletionMessage();
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// 사용 예
const tracker = new OrderTracker(10);
tracker.connect();
```

---

## 주요 차이점

| 구분 | 점주용 (Route) | 고객용 (Order) |
|-----|--------------|---------------|
| **구독 경로** | `/topic/route/{routeId}` | `/topic/order/{orderId}/position` |
| **필요 정보** | RouteId | OrderId |
| **추적 범위** | 전체 배송 경로 (여러 주문) | 자신의 주문만 |
| **위치 업데이트** | 항상 | 자신의 배송지로 향할 때만 |
| **사용자** | 점주, 관리자 | 일반 고객 |

---

## 위치 업데이트 주기

- **업데이트 간격**: 2초마다
- **업데이트 조건**:
  - 점주용: 모든 위치 업데이트
  - 고객용: 자신의 배송이 완료될 때까지 계속 업데이트
    - 매장 출발 → 첫 번째 배송지 → ... → 자신의 배송지 도착까지 모두 추적
    - 자신의 배송이 완료되면 더 이상 업데이트되지 않음

---

## 주의사항

1. **WebSocket 연결 타이밍**
   - 주문 생성 후 바로 연결해도 됨
   - 배송이 시작되면 자동으로 위치 정보 수신 시작

2. **배송이 시작되지 않은 경우**
   - WebSocket은 연결되지만 위치 정보는 수신되지 않음
   - 주문 상태 API로 현재 상태 확인 가능

3. **여러 주문이 같이 배송되는 경우**
   - 각 고객은 매장 출발부터 자신의 배송 완료까지 모든 위치 업데이트 수신
   - 예시:
     - 배송 순서: 매장 → A집 → B집 → C집
     - A 고객: 매장 → A집 도착까지 추적 (B집, C집 이동은 안 보임)
     - B 고객: 매장 → A집 → B집 도착까지 추적 (C집 이동은 안 보임)
     - C 고객: 매장 → A집 → B집 → C집 도착까지 모두 추적

4. **배터리 정보**
   - 모든 구독자에게 동일한 배터리 정보 전송
   - 실시간 소모 상황 확인 가능

5. **실시간 상태 업데이트**
   - **RouteStop 상태**: 각 경유지 도착 시 즉시 업데이트됨 (PENDING → ARRIVED → DEPARTED)
   - **Order 상태**: 각 주문 배송 완료 시 즉시 업데이트됨 (ASSIGNED → FULFILLED)
   - 전체 배송이 끝날 때까지 기다리지 않고, 각 경유지마다 DB에 즉시 반영됨
   - 데이터베이스를 실시간으로 조회하면 배송 진행 상황을 정확히 확인할 수 있음

---

## 기술 구현 세부 사항

### 실시간 트랜잭션 처리

배송 시뮬레이션은 비동기로 동작하며, 각 경유지마다 독립적인 트랜잭션으로 처리됩니다.

#### 아키텍처
```
DroneSimulatorService (메인 시뮬레이션)
    └─> RouteStopProcessingService (경유지별 처리)
            ├─> RouteStop 상태 업데이트 (ARRIVED)
            ├─> 3초 대기 (배송 시뮬레이션)
            ├─> RouteStop 상태 업데이트 (DEPARTED)
            └─> Order 상태 업데이트 (FULFILLED)
```

#### 트랜잭션 전략
1. **메인 시뮬레이션**: `@Transactional(REQUIRES_NEW)` - 전체 경로 관리
2. **경유지 처리**: `@Transactional(REQUIRES_NEW)` - 각 경유지마다 독립 트랜잭션
3. **즉시 커밋**: `flush()` 호출로 DB에 즉시 반영

이러한 구조 덕분에:
- ✅ 각 경유지 도착 즉시 DB에 상태 반영
- ✅ 전체 배송 완료를 기다리지 않음
- ✅ 실시간으로 배송 진행 상황 추적 가능

#### WebSocket 메시지 흐름
```
[드론 이동]
    ↓ (2초마다)
[RoutePosition 저장]
    ↓
[WebSocket 브로드캐스트]
    ├─> /topic/route/{routeId} (점주용)
    └─> /topic/order/{orderId}/position (고객용 - 배송 전 주문만)

[경유지 도착]
    ↓
[RouteStop.arrive() + flush()]
    ↓
[3초 대기]
    ↓
[RouteStop.depart() + Order.completeDelivery() + flush()]
    ↓
[WebSocket 알림]
    └─> /topic/order/{orderId} (배송 완료)
```

---

## 테스트 방법

### 1. 주문 생성
```bash
POST http://localhost:8080/api/orders
{
    "storeId": 1,
    "userId": 1,
    "items": [...]
}
```

### 2. HTML 파일 열기
- 고객용: `client-examples/customer-tracking.html`
- 점주용: `client-examples/drone-tracking.html`

### 3. 배송 시작
```bash
POST http://localhost:8080/api/routes/start-delivery
{
    "orderIds": [10, 11, 12]
}
```

### 4. 실시간 추적 확인
- 브라우저 콘솔과 화면에서 위치 정보 확인

---

## 데이터베이스 상태 조회

배송 진행 중 데이터베이스를 실시간으로 조회하여 상태를 확인할 수 있습니다.

### RouteStop 상태 확인
```sql
SELECT rs.stop_id, rs.stop_type, rs.status, rs.lat, rs.lng, rs.sequence
FROM route_stop rs
WHERE rs.route_id = {routeId}
ORDER BY rs.sequence;
```

**상태 값**:
- `PENDING`: 아직 도착하지 않음
- `ARRIVED`: 도착 완료 (배송 중)
- `DEPARTED`: 출발 완료

### Order 상태 확인
```sql
SELECT o.order_id, o.status, u.name as customer_name, o.total_price
FROM orders o
JOIN user u ON o.user_id = u.user_id
WHERE o.order_id IN ({orderIds});
```

**상태 값**:
- `CREATED`: 주문 생성됨
- `ASSIGNED`: 배송이 시작됨
- `FULFILLED`: 배송 완료
- `CANCELED`: 취소됨
- `FAILED`: 배송 실패

### 드론 위치 이력 확인
```sql
SELECT rp.position_id, rp.lat, rp.lng, rp.battery_pct, rp.ts
FROM route_position rp
WHERE rp.route_id = {routeId}
ORDER BY rp.ts DESC
LIMIT 10;
```

---

## 문제 해결

### WebSocket 연결 실패
- CORS 설정 확인 (현재 모든 origin 허용)
- 서버 실행 여부 확인
- 포트 확인 (기본: 8080)

### 위치 정보가 수신되지 않음
- 배송이 시작되었는지 확인
- 고객용: 현재 드론이 자신의 배송지로 향하는지 확인
- 주문 ID가 올바른지 확인

### 배송 완료 알림이 오지 않음
- `/topic/order/{orderId}` 구독 여부 확인
- 서버 로그에서 배송 완료 처리 확인

### 상태가 업데이트되지 않는 것처럼 보임
- 데이터베이스를 직접 조회하면 각 경유지마다 실시간으로 상태가 변경되는 것을 확인할 수 있음
- WebSocket 메시지는 수신하지만 DB 상태는 각 경유지 처리 완료 시 즉시 커밋됨
- 서버 로그에서 "Stop 처리 완료 및 커밋됨" 메시지 확인
