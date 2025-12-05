# 데이터베이스 제약조건 명세서

> 본 문서는 드론 배송 시스템의 데이터베이스에 정의된 제약조건을 상세히 기술합니다.
> 단순 기본키(PRIMARY KEY) 제약조건은 제외하고, 비즈니스 로직과 데이터 무결성을 보장하는 주요 제약조건을 중심으로 정리했습니다.

---

## 1. 외래키 제약조건 (Foreign Key Constraints)

### 1.1 User 테이블
- 제약조건 없음 (최상위 엔티티)

### 1.2 Store 테이블
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `owner_id` | `user` | `user_id` | 점주(사용자)와 매장을 연결. NOT NULL 제약으로 반드시 점주가 존재해야 함 |

**비즈니스 의미**: 모든 매장은 반드시 점주가 있어야 하며, 점주 정보 삭제 시 해당 매장의 처리 정책 필요

### 1.3 Drone 테이블
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `store_id` | `store` | `store_id` | 드론이 소속된 매장. NOT NULL 제약으로 반드시 매장에 소속되어야 함 |

**비즈니스 의미**: 모든 드론은 특정 매장에 귀속되며, 독립적으로 존재할 수 없음

### 1.4 Orders 테이블
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `store_id` | `store` | `store_id` | 주문이 접수된 매장. NOT NULL |
| `user_id` | `user` | `user_id` | 주문한 고객. NOT NULL |

**비즈니스 의미**: 주문은 반드시 특정 매장과 고객에게 귀속됨

### 1.5 OrderItem 테이블
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `order_id` | `orders` | `order_id` | 주문 항목이 속한 주문. NOT NULL |
| `product_id` | `product` | `product_id` | 주문한 상품. NOT NULL |

**CASCADE 옵션**: `orphanRemoval = true`로 주문 삭제 시 주문 항목도 함께 삭제됨

### 1.6 StoreProduct 테이블 (복합키)
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `store_id` | `store` | `store_id` | 복합키의 일부. NOT NULL |
| `product_id` | `product` | `product_id` | 복합키의 일부. NOT NULL |

**특징**: `@IdClass`를 사용한 복합키 구조로, 매장-상품 조합이 유일해야 함

### 1.7 Route 테이블
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `drone_id` | `drone` | `drone_id` | 배송 경로를 수행하는 드론. NOT NULL |
| `store_id` | `store` | `store_id` | 배송 경로의 출발 매장. NOT NULL |

**비즈니스 의미**: 배송 경로는 반드시 특정 드론과 매장에 연결되어야 함

### 1.8 RouteStop 테이블
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `route_id` | `route` | `route_id` | 정류장이 속한 배송 경로. NOT NULL |
| `store_id` | `store` | `store_id` | 픽업 정류장인 경우 매장 참조. NULLABLE |
| `user_id` | `user` | `user_id` | 배달 정류장인 경우 고객 참조. NULLABLE |

**CASCADE 옵션**: `orphanRemoval = true`로 경로 삭제 시 정류장도 함께 삭제됨
**특징**: `store_id`와 `user_id` 중 하나만 설정됨 (정류장 유형에 따라)

### 1.9 RouteStopOrder 테이블
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `stop_id` | `route_stop` | `stop_id` | 정류장. NOT NULL |
| `order_id` | `orders` | `order_id` | 해당 정류장에서 처리되는 주문. NOT NULL |

**CASCADE 옵션**: `orphanRemoval = true`로 정류장 삭제 시 매핑도 삭제됨

### 1.10 RoutePosition 테이블
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `route_id` | `route` | `route_id` | 위치가 기록된 경로. NOT NULL |
| `stop_from_id` | `route_stop` | `stop_id` | 출발 정류장. NULLABLE |
| `stop_to_id` | `route_stop` | `stop_id` | 도착 정류장. NULLABLE |

**CASCADE 옵션**: `orphanRemoval = true`로 경로 삭제 시 위치 기록도 삭제됨

### 1.11 FlightLog 테이블
| 컬럼명 | 참조 테이블 | 참조 컬럼 | 설명 |
|--------|------------|----------|------|
| `route_id` | `route` | `route_id` | 비행 로그가 속한 경로. NOT NULL |
| `drone_id` | `drone` | `drone_id` | 비행을 수행한 드론. NOT NULL |

**CASCADE 옵션**: `orphanRemoval = true`로 경로 삭제 시 비행 로그도 삭제됨

---

## 2. NOT NULL 제약조건 (필수 데이터)

### 2.1 User 테이블
- `name`: 사용자 이름 (필수)
- `lat`, `lng`: 위치 좌표 (필수, 배송지 계산에 필수적)
- `role`: 사용자 역할 (CUSTOMER/OWNER, 필수)
- `registered_at`: 등록 일시 (필수, 자동 생성)

### 2.2 Store 테이블
- `owner_id`: 점주 ID (필수)
- `name`: 매장명 (필수)
- `type`: 매장 유형 (필수)
- `lat`, `lng`: 매장 위치 (필수)
- `delivery_radius_km`: 배송 가능 거리 (필수, 기본값 2.00km)
- `is_active`: 활성화 상태 (필수, 기본값 true)
- `registered_at`: 등록 일시 (필수)

### 2.3 Drone 테이블
- `store_id`: 소속 매장 (필수)
- `model`: 드론 모델명 (필수)
- `battery_capacity`: 배터리 용량 (필수, mAh 단위)
- `max_payload_kg`: 최대 적재 중량 (필수)
- `status`: 드론 상태 (필수, 기본값 IDLE)
- `registered_at`: 등록 일시 (필수)

### 2.4 Product 테이블
- `name`: 상품명 (필수)
- `category`: 카테고리 (필수)
- `unit_weight_kg`: 단위 중량 (필수, 배송 계산에 필수적)
- `requires_verification`: 신분 확인 필요 여부 (필수, 기본값 false)
- `is_active`: 활성화 상태 (필수, 기본값 true)

### 2.5 StoreProduct 테이블
- `store_id`, `product_id`: 복합키 (필수)
- `price`: 판매 가격 (필수)
- `stock_qty`: 재고 수량 (필수, 기본값 0)
- `max_qty_per_order`: 1회 주문 최대 수량 (필수, 기본값 10)
- `is_active`: 판매 활성화 상태 (필수, 기본값 true)

### 2.6 Orders 테이블
- `store_id`, `user_id`: 주문 관계 (필수)
- `origin_lat`, `origin_lng`: 출발지 좌표 (필수)
- `dest_lat`, `dest_lng`: 도착지 좌표 (필수)
- `total_weight_kg`: 총 중량 (필수)
- `total_amount`: 총 금액 (필수)
- `item_count`: 항목 개수 (필수)
- `status`: 주문 상태 (필수, 기본값 CREATED)
- `created_at`: 주문 생성 일시 (필수)

### 2.7 OrderItem 테이블
- `order_id`, `product_id`: 주문-상품 관계 (필수)
- `quantity`: 수량 (필수)
- `unit_price`: 단가 (필수)
- `unit_weight_kg`: 단위 중량 (필수)

### 2.8 Route 테이블
- `drone_id`, `store_id`: 드론-매장 관계 (필수)
- `status`: 경로 상태 (필수, 기본값 PLANNED)

### 2.9 RouteStop 테이블
- `route_id`: 경로 (필수)
- `stop_sequence`: 정류장 순서 (필수, 경로 최적화에 필수적)
- `type`: 정류장 유형 (PICKUP/DROPOFF, 필수)
- `lat`, `lng`: 정류장 위치 (필수)
- `status`: 정류장 상태 (필수, 기본값 PENDING)

### 2.10 RouteStopOrder 테이블
- `stop_id`, `order_id`: 정류장-주문 관계 (필수)
- `created_at`: 생성 일시 (필수)

### 2.11 RoutePosition 테이블
- `route_id`: 경로 (필수)
- `lat`, `lng`: 드론 위치 (필수)
- `ts`: 타임스탬프 (필수)

### 2.12 FlightLog 테이블
- `route_id`, `drone_id`: 경로-드론 관계 (필수)
- `start_time`, `end_time`: 비행 시작/종료 시각 (필수)
- `distance`: 비행 거리 (필수)
- `battery_used`: 사용된 배터리 (필수)
- `result`: 비행 결과 (필수)

---

## 3. ENUM 제약조건 (열거형 값 제한)

### 3.1 UserRole (사용자 역할)
**적용 테이블**: `user`
**허용 값**:
- `CUSTOMER`: 고객
- `OWNER`: 점주

**제약 의미**: 사용자는 고객 또는 점주 중 하나의 역할만 가질 수 있음

### 3.2 StoreType (매장 유형)
**적용 테이블**: `store`
**허용 값**:
- `RESTAURANT`: 레스토랑
- `GROCERY`: 식료품점
- `PHARMACY`: 약국
- `CONVENIENCE`: 편의점
- `OTHERS`: 기타

**제약 의미**: 매장은 정의된 유형 중 하나로만 분류됨

### 3.3 DroneStatus (드론 상태)
**적용 테이블**: `drone`
**허용 값**:
- `IDLE`: 대기 중
- `FLYING`: 비행 중
- `CHARGING`: 충전 중
- `MAINTENANCE`: 정비 중

**제약 의미**: 드론의 현재 상태를 명확히 구분하여 배송 할당 가능 여부 판단

### 3.4 OrderStatus (주문 상태)
**적용 테이블**: `orders`
**허용 값**:
- `CREATED`: 생성됨
- `ASSIGNED`: 배송 할당됨
- `FULFILLED`: 배송 완료
- `CANCELED`: 취소됨
- `FAILED`: 실패

**제약 의미**: 주문의 라이프사이클을 명확히 관리

### 3.5 RouteStatus (경로 상태)
**적용 테이블**: `route`
**허용 값**:
- `PLANNED`: 계획됨
- `LAUNCHED`: 출발함
- `COMPLETED`: 완료됨
- `ABORTED`: 중단됨

**제약 의미**: 배송 경로의 진행 상태를 추적

### 3.6 StopType (정류장 유형)
**적용 테이블**: `route_stop`
**허용 값**:
- `PICKUP`: 픽업 (매장에서 상품 수거)
- `DROPOFF`: 배달 (고객에게 배달)

**제약 의미**: 정류장의 목적을 명확히 구분

### 3.7 StopStatus (정류장 상태)
**적용 테이블**: `route_stop`
**허용 값**:
- `PENDING`: 대기 중
- `ARRIVED`: 도착함
- `DEPARTED`: 출발함
- `SKIPPED`: 건너뜀

**제약 의미**: 각 정류장별 배송 진행 상황 추적

### 3.8 FlightResult (비행 결과)
**적용 테이블**: `flight_log`
**허용 값**:
- `SUCCESS`: 성공
- `PARTIAL`: 부분 성공
- `FAILED`: 실패

**제약 의미**: 비행 로그의 결과를 명확히 기록

---

## 4. 숫자 정밀도 제약조건 (Precision & Scale)

### 4.1 좌표 데이터 (Latitude, Longitude)
**적용 테이블**: `user`, `store`, `orders`, `route_stop`, `route_position`
**제약**: `DECIMAL(9, 6)`
- **의미**: 전체 9자리, 소수점 이하 6자리
- **범위**: -180.000000 ~ 180.000000
- **정밀도**: 약 11cm 단위까지 표현 (GPS 정밀도 확보)

**비즈니스 의미**: 드론 배송의 정확한 위치 기반 서비스 제공

### 4.2 중량 데이터 (Weight in kg)
**적용 컬럼**:
- `product.unit_weight_kg`: DECIMAL(6, 3)
- `order_item.unit_weight_kg`: DECIMAL(6, 3)
- `orders.total_weight_kg`: DECIMAL(8, 3)
- `drone.max_payload_kg`: DECIMAL(6, 3)
- `route.planned_total_payload_kg`: DECIMAL(8, 3)
- `route_stop.payload_delta_kg`: DECIMAL(7, 3)

**제약 의미**:
- 상품/드론 중량: 최대 999.999kg까지 표현 (그램 단위 정밀도)
- 주문 총 중량: 최대 99,999.999kg (다중 주문 합산 가능)
- 정류장 중량 변화: -9,999.999 ~ 9,999.999kg (적재/하차 표현)

### 4.3 거리 데이터 (Distance in km)
**적용 컬럼**:
- `store.delivery_radius_km`: DECIMAL(5, 2)
- `route.planned_total_distance_km`: DECIMAL(8, 3)
- `flight_log.distance`: DECIMAL(8, 3)

**제약 의미**:
- 배송 반경: 최대 999.99km (10m 단위)
- 비행 거리: 최대 99,999.999km (미터 단위 정밀도)

### 4.4 속도 및 배터리 데이터
**적용 컬럼**:
- `route_position.speed_mps`: DECIMAL(6, 2) - 최대 9,999.99 m/s
- `route_position.battery_pct`: DECIMAL(5, 2) - 0.00 ~ 100.00%

**제약 의미**: 실시간 드론 상태 모니터링의 정밀도 확보

---

## 5. 길이 제약조건 (Length Constraints)

### 5.1 짧은 텍스트 필드
| 컬럼 | 테이블 | 최대 길이 | 용도 |
|------|--------|----------|------|
| `phone` | `user`, `store` | 20 | 전화번호 (국제 형식 포함) |
| `role` | `user` | 20 | 사용자 역할 ENUM |
| `type` | `store` | 20 | 매장 유형 ENUM |
| `heuristic` | `route` | 40 | 경로 최적화 알고리즘명 |
| `model` | `drone` | 40 | 드론 모델명 |
| `category` | `product` | 60 | 상품 카테고리 |

### 5.2 중간 길이 텍스트 필드
| 컬럼 | 테이블 | 최대 길이 | 용도 |
|------|--------|----------|------|
| `name` | `user` | 80 | 사용자 이름 |
| `name` | `store` | 100 | 매장명 |
| `name` | `route_stop` | 120 | 정류장 이름 |
| `name` | `product` | 120 | 상품명 |

### 5.3 주소 필드
| 컬럼 | 테이블 | 최대 길이 | 용도 |
|------|--------|----------|------|
| `address` | `user`, `store` | 200 | 도로명 주소 |

### 5.4 긴 텍스트 필드 (TEXT)
**적용 컬럼**:
- `orders.failure_reason`: 주문 실패 사유
- `orders.note`: 주문 메모
- `route.note`: 경로 메모
- `route_stop.note`: 정류장 메모
- `flight_log.note`: 비행 로그 메모

**제약 의미**: `columnDefinition = "TEXT"`로 길이 제한 없음 (최대 64KB)

---

## 6. 기본값 제약조건 (Default Values)

### 6.1 Boolean 플래그
| 컬럼 | 테이블 | 기본값 | 의미 |
|------|--------|--------|------|
| `is_active` | `store` | `true` | 매장 활성화 상태 (기본 활성) |
| `is_active` | `product` | `true` | 상품 활성화 상태 (기본 활성) |
| `is_active` | `store_product` | `true` | 판매 활성화 상태 (기본 활성) |
| `requires_verification` | `product` | `false` | 신분 확인 불필요 (기본값) |

### 6.2 상태 ENUM
| 컬럼 | 테이블 | 기본값 | 의미 |
|------|--------|--------|------|
| `status` | `drone` | `IDLE` | 드론 초기 상태는 대기 |
| `status` | `orders` | `CREATED` | 주문 생성 시 초기 상태 |
| `status` | `route` | `PLANNED` | 경로 생성 시 초기 상태 |
| `status` | `route_stop` | `PENDING` | 정류장 초기 상태는 대기 |

### 6.3 숫자 기본값
| 컬럼 | 테이블 | 기본값 | 의미 |
|------|--------|--------|------|
| `delivery_radius_km` | `store` | `2.00` | 기본 배송 반경 2km |
| `stock_qty` | `store_product` | `0` | 초기 재고 없음 |
| `max_qty_per_order` | `store_product` | `10` | 1회 주문 최대 10개 |

---

## 7. 자동 생성 타임스탬프 제약조건

### 7.1 @PrePersist 자동 생성
다음 컬럼들은 엔티티 생성 시 자동으로 현재 시각이 설정됨:
- `user.registered_at`
- `store.registered_at`
- `drone.registered_at`
- `orders.created_at`
- `route_stop_order.created_at`
- `route_position.ts`

**제약 특징**: `updatable = false` 속성으로 수정 불가능

### 7.2 업무 로직으로 관리되는 타임스탬프
다음 컬럼들은 비즈니스 로직에서 상태 변경 시 설정됨:
- `orders.assigned_at`: 배송 할당 시
- `orders.completed_at`: 배송 완료 시
- `orders.canceled_at`: 주문 취소 시
- `route.actual_start_at`: 경로 시작 시
- `route.actual_end_at`: 경로 완료 시
- `route_stop.actual_arrival_at`: 정류장 도착 시
- `route_stop.actual_departure_at`: 정류장 출발 시

---

## 8. 복합키 제약조건

### 8.1 StoreProduct 복합키
**클래스**: `StoreProduct.StoreProductId`
**구성**: `(store_id, product_id)`

**제약 의미**:
- 동일 매장에서 동일 상품은 하나의 레코드만 존재 가능
- 매장별로 상품 가격과 재고가 독립적으로 관리됨

**Java 코드**:
```java
@IdClass(StoreProduct.StoreProductId.class)
public class StoreProduct {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
```

---

## 9. 업데이트 불가 제약조건 (Immutable Fields)

다음 컬럼들은 `updatable = false` 속성으로 한 번 설정되면 수정할 수 없음:

| 컬럼 | 테이블 | 이유 |
|------|--------|------|
| `registered_at` | `user`, `store`, `drone` | 등록 일시는 불변 |
| `created_at` | `orders`, `route_stop_order` | 생성 일시는 불변 |

---

## 10. CASCADE 및 Orphan Removal 제약조건

### 10.1 양방향 관계에서의 자동 삭제
다음 관계들은 부모 엔티티 삭제 시 자식 엔티티도 자동 삭제됨:

| 부모 엔티티 | 자식 엔티티 | 설정 |
|------------|------------|------|
| `Order` | `OrderItem` | `cascade = CascadeType.ALL, orphanRemoval = true` |
| `Route` | `RouteStop` | `cascade = CascadeType.ALL, orphanRemoval = true` |
| `Route` | `RoutePosition` | `cascade = CascadeType.ALL, orphanRemoval = true` |
| `Route` | `FlightLog` | `cascade = CascadeType.ALL, orphanRemoval = true` |
| `RouteStop` | `RouteStopOrder` | `cascade = CascadeType.ALL, orphanRemoval = true` |

**비즈니스 의미**:
- 주문 삭제 시 주문 항목도 함께 삭제
- 경로 삭제 시 정류장, 위치 기록, 비행 로그 모두 삭제
- 정류장 삭제 시 해당 정류장의 주문 매핑도 삭제

---

## 11. 비�니스 규칙 기반 묵시적 제약조건

### 11.1 RouteStop의 참조 제약
**규칙**: `store_id`와 `user_id`는 모두 NULLABLE이지만, 둘 중 하나는 반드시 설정되어야 함
- `stop_type = PICKUP` → `store_id` 필수, `user_id` NULL
- `stop_type = DROPOFF` → `user_id` 필수, `store_id` NULL

**Java 레벨 검증 필요**: 데이터베이스 레벨 CHECK 제약은 없으나, 애플리케이션 레벨에서 검증 권장

### 11.2 재고 감소 제약
**클래스**: `StoreProduct.decreaseStock(int quantity)`
**규칙**: 재고 수량은 0 미만으로 감소할 수 없음

```java
public void decreaseStock(int quantity) {
    // 애플리케이션 레벨 검증 필요
    if (this.stockQty < quantity) {
        throw new IllegalStateException("재고 부족");
    }
    this.stockQty -= quantity;
}
```

### 11.3 드론 최대 적재량 제약
**규칙**: `route.planned_total_payload_kg`는 `drone.max_payload_kg`를 초과할 수 없음
- 애플리케이션 레벨에서 경로 계획 시 검증 필요

### 11.4 배송 가능 거리 제약
**규칙**: 주문의 `dest_lat`, `dest_lng`는 `store`의 위치에서 `delivery_radius_km` 내에 있어야 함
- 애플리케이션 레벨에서 주문 생성 시 검증 필요

---

## 12. 제약조건 요약표

| 제약조건 유형 | 개수 | 주요 목적 |
|--------------|------|----------|
| 외래키 (FK) | 17개 | 참조 무결성 보장 |
| NOT NULL | 60개 이상 | 필수 데이터 보장 |
| ENUM 제약 | 8개 | 허용 값 제한 |
| 정밀도 제약 | 20개 이상 | 숫자 데이터 정확성 |
| 길이 제약 | 15개 이상 | 문자열 크기 제한 |
| 기본값 | 12개 | 일관된 초기 상태 |
| 복합키 | 1개 | 유일성 보장 |
| CASCADE | 5개 | 데이터 정합성 유지 |
| 불변 필드 | 7개 | 이력 추적 보장 |

---

## 13. 제약조건 검증 권장사항

### 13.1 데이터베이스 레벨 추가 권장 제약
1. **CHECK 제약조건 추가**:
   ```sql
   ALTER TABLE drone ADD CONSTRAINT chk_battery_capacity
   CHECK (battery_capacity > 0);

   ALTER TABLE drone ADD CONSTRAINT chk_max_payload
   CHECK (max_payload_kg > 0);

   ALTER TABLE store_product ADD CONSTRAINT chk_price
   CHECK (price >= 0);

   ALTER TABLE store_product ADD CONSTRAINT chk_stock
   CHECK (stock_qty >= 0);
   ```

2. **UNIQUE 제약조건 추가**:
   ```sql
   -- 드론 모델 + 매장 조합이 유일하도록 (선택적)
   ALTER TABLE drone ADD CONSTRAINT uk_store_model
   UNIQUE (store_id, model);
   ```

### 13.2 애플리케이션 레벨 검증 권장
1. RouteStop의 store_id/user_id 상호 배타적 검증
2. 드론 적재량 초과 검증
3. 배송 가능 거리 검증
4. 재고 부족 검증
5. 주문 상태 전이 규칙 검증 (CREATED → ASSIGNED → FULFILLED)
6. 경로 상태 전이 규칙 검증 (PLANNED → LAUNCHED → COMPLETED)

---

## 14. 참고사항

### 14.1 JPA 전략
- **ID 생성 전략**: 모든 엔티티가 `IDENTITY` 전략 사용 (AUTO_INCREMENT)
- **Fetch 전략**: 모든 @ManyToOne, @OneToMany 관계는 `LAZY` 로딩 사용 (N+1 방지)
- **Cascade 전략**: 집합 관계(Aggregate)에서만 `CascadeType.ALL` 사용

### 14.2 데이터 타입 정책
- **날짜/시간**: Java 8 `LocalDateTime` 사용 (타임존 없음)
- **금액**: `Integer` 타입 사용 (원 단위)
- **좌표/중량/거리**: `BigDecimal` 사용 (정밀도 보장)
- **열거형**: Java Enum + `@Enumerated(EnumType.STRING)` 사용

---

**문서 버전**: 1.0
**최종 수정일**: 2025-12-04
**작성자**: Claude Code Agent
