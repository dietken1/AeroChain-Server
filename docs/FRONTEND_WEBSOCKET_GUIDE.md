# 드론 실시간 추적 웹소켓 연동 가이드 (프론트엔드)

## 목차
1. [개요](#개요)
2. [백엔드 구현 현황](#백엔드-구현-현황)
3. [WebSocket 연결 방법](#websocket-연결-방법)
4. [데이터 형식](#데이터-형식)
5. [카카오맵 연동 예시](#카카오맵-연동-예시)
6. [전체 코드 예시](#전체-코드-예시)
7. [주의사항 및 팁](#주의사항-및-팁)

---

## 개요

점주가 **배송 시작 버튼**을 누르면 백엔드에서 다음과 같은 작업이 자동으로 수행됩니다:

1. `POST /api/routes/start-delivery` API 호출
2. 드론 시뮬레이터 자동 시작
3. **2초마다** 드론 위치를 DB에 저장
4. **2초마다** WebSocket을 통해 드론 위치 정보 전송

프론트엔드에서는 WebSocket에 연결하여 실시간으로 드론 위치를 받아 카카오맵에 표시하면 됩니다.

---

## 백엔드 구현 현황

### API 엔드포인트

#### 1. 배송 시작
```
POST https://drone-server.p-e.kr/api/routes/start-delivery
Content-Type: application/json

{
  "orderIds": [1, 2, 3]  // 배송할 주문 ID 리스트 (최대 3개)
}
```

**응답 (성공):**
```
HTTP/1.1 200 OK
배송이 성공적으로 시작되었습니다.
```

#### 2. 경로 조회
```
GET https://drone-server.p-e.kr/api/routes/{routeId}
```

#### 3. 드론 현재 위치 조회
```
GET https://drone-server.p-e.kr/api/routes/{routeId}/current-position
```

**응답 예시:**
```json
{
  "lat": 37.497900,
  "lng": 127.027600,
  "speedMps": 8.33,
  "batteryPct": 95.5,
  "timestamp": "2025-01-15T12:34:56"
}
```

### WebSocket 설정

- **엔드포인트:** `https://drone-server.p-e.kr/ws` (HTTPS 배포 서버)
- **프로토콜:** SockJS + STOMP
- **구독 토픽:** `/topic/route/{routeId}`
- **업데이트 주기:** 2초
- **로컬 개발:** 백엔드에 이미 `http://localhost:3000`, `http://localhost:5173` CORS 설정 완료 ✅

---

## WebSocket 연결 방법

### 1. 필수 라이브러리 설치

#### CDN 사용 (HTML)
```html
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
```

#### NPM 사용 (React, Vue 등)
```bash
npm install sockjs-client stompjs
```

### 2. WebSocket 연결 코드

```javascript
let stompClient = null;
let currentSubscription = null;
const WS_URL = 'https://drone-server.p-e.kr/ws';  // HTTPS 사용

// WebSocket 연결
function connectWebSocket(routeId) {
  // SockJS 소켓 생성
  const socket = new SockJS(WS_URL);
  stompClient = Stomp.over(socket);

  // 연결
  stompClient.connect({}, function(frame) {
    console.log('WebSocket 연결 성공!');

    // 토픽 구독
    currentSubscription = stompClient.subscribe('/topic/route/' + routeId, function(message) {
      const data = JSON.parse(message.body);
      handleDronePosition(data);  // 드론 위치 처리 함수
    });

    console.log(`Route ${routeId} 구독 시작`);
  }, function(error) {
    console.error('WebSocket 연결 실패:', error);
  });
}

// WebSocket 연결 해제
function disconnectWebSocket() {
  if (stompClient && stompClient.connected) {
    if (currentSubscription) {
      currentSubscription.unsubscribe();
    }
    stompClient.disconnect(function() {
      console.log('WebSocket 연결 해제됨');
    });
  }
}
```

---

## 데이터 형식

WebSocket을 통해 2초마다 전송되는 데이터 형식:

```json
{
  "routeId": 1,              // 경로 ID
  "lat": 37.497900,          // 위도 (double)
  "lng": 127.027600,         // 경도 (double)
  "speed": 30.0,             // 속도 (km/h)
  "battery": 95.5,           // 배터리 잔량 (%)
  "timestamp": "2025-01-15T12:34:56"  // 타임스탬프
}
```

---

## 카카오맵 연동 예시

### 1. 카카오맵 API 설정

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>드론 실시간 추적</title>
  <!-- 카카오맵 API -->
  <script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_APP_KEY"></script>
  <!-- WebSocket 라이브러리 -->
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
  <div id="map" style="width:100%;height:600px;"></div>
</body>
</html>
```

### 2. 지도 및 마커 초기화

```javascript
let map;
let droneMarker;
let pathLine;
let pathCoordinates = [];

// 페이지 로드 시 지도 초기화
function initMap() {
  const container = document.getElementById('map');
  const options = {
    center: new kakao.maps.LatLng(37.4979, 127.0276),  // 초기 중심 좌표
    level: 3  // 확대 레벨
  };

  map = new kakao.maps.Map(container, options);

  // 드론 마커 생성
  const markerPosition = new kakao.maps.LatLng(37.4979, 127.0276);
  droneMarker = new kakao.maps.Marker({
    position: markerPosition,
    map: map
  });

  // 커스텀 아이콘 설정 (선택사항)
  const imageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png';
  const imageSize = new kakao.maps.Size(64, 69);
  const imageOption = { offset: new kakao.maps.Point(27, 69) };
  const markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);
  droneMarker.setImage(markerImage);

  // 경로 선 초기화
  pathLine = new kakao.maps.Polyline({
    path: pathCoordinates,
    strokeWeight: 3,
    strokeColor: '#0066ff',
    strokeOpacity: 0.7,
    strokeStyle: 'solid'
  });
  pathLine.setMap(map);
}
```

### 3. 드론 위치 업데이트 처리

```javascript
// WebSocket에서 드론 위치 데이터 수신 시 호출
function handleDronePosition(data) {
  console.log('드론 위치 업데이트:', data);

  // 1. UI 업데이트 (위도, 경도, 속도, 배터리)
  document.getElementById('latValue').textContent = data.lat.toFixed(6);
  document.getElementById('lngValue').textContent = data.lng.toFixed(6);
  document.getElementById('speedValue').textContent = data.speed.toFixed(1);
  document.getElementById('batteryValue').textContent = data.battery.toFixed(1);

  // 2. 배터리 바 업데이트
  updateBatteryBar(data.battery);

  // 3. 지도 마커 업데이트 (부드러운 애니메이션)
  updateMapMarker(data.lat, data.lng);

  // 4. 경로에 추가
  addToPath(data.lat, data.lng);
}

// 배터리 바 업데이트
function updateBatteryBar(battery) {
  const batteryBar = document.getElementById('batteryBar');
  batteryBar.style.width = battery + '%';

  if (battery > 50) {
    batteryBar.className = 'battery-fill';  // 녹색
  } else if (battery > 20) {
    batteryBar.className = 'battery-fill warning';  // 노란색
  } else {
    batteryBar.className = 'battery-fill danger';  // 빨간색
  }
}

// 지도 마커 위치 업데이트 (부드러운 애니메이션)
function updateMapMarker(lat, lng) {
  smoothMoveMarker(lat, lng);

  // 지도 중심 이동
  const position = new kakao.maps.LatLng(lat, lng);
  map.panTo(position);
}

// 부드러운 마커 이동 애니메이션
function smoothMoveMarker(lat, lng, duration = 1500) {
  const startPosition = droneMarker.getPosition();
  const endPosition = new kakao.maps.LatLng(lat, lng);
  const startTime = Date.now();

  function animate() {
    const elapsed = Date.now() - startTime;
    const progress = Math.min(elapsed / duration, 1);

    const currentLat = startPosition.getLat() + (endPosition.getLat() - startPosition.getLat()) * progress;
    const currentLng = startPosition.getLng() + (endPosition.getLng() - startPosition.getLng()) * progress;

    droneMarker.setPosition(new kakao.maps.LatLng(currentLat, currentLng));

    if (progress < 1) {
      requestAnimationFrame(animate);
    }
  }

  animate();
}

// 드론 이동 경로에 추가
function addToPath(lat, lng) {
  pathCoordinates.push(new kakao.maps.LatLng(lat, lng));
  pathLine.setPath(pathCoordinates);
}
```

---

## 전체 코드 예시

### HTML
```html
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>드론 실시간 추적</title>

  <!-- 카카오맵 API -->
  <script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_APP_KEY"></script>

  <!-- WebSocket 라이브러리 -->
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

  <style>
    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; }
    #map { width: 100%; height: 600px; border-radius: 8px; }
    .info-panel { margin-top: 20px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
    .info-card { padding: 15px; background: #f8f9fa; border-radius: 4px; }
    .info-label { font-size: 12px; color: #666; }
    .info-value { font-size: 24px; font-weight: bold; color: #333; }
    .battery-bar { height: 20px; background: #e0e0e0; border-radius: 4px; overflow: hidden; margin-top: 5px; }
    .battery-fill { height: 100%; background: #28a745; transition: width 0.3s ease, background-color 0.3s ease; }
    .battery-fill.warning { background: #ffc107; }
    .battery-fill.danger { background: #dc3545; }
  </style>
</head>
<body>
  <h1>드론 실시간 추적 시스템</h1>

  <!-- 제어 버튼 -->
  <div>
    <input type="number" id="routeIdInput" placeholder="Route ID" value="1">
    <button onclick="connectWebSocket()">WebSocket 연결</button>
    <button onclick="disconnectWebSocket()">연결 해제</button>
  </div>

  <!-- 지도 -->
  <div id="map"></div>

  <!-- 드론 정보 패널 -->
  <div class="info-panel">
    <div class="info-card">
      <div class="info-label">위도 (Latitude)</div>
      <div class="info-value" id="latValue">-</div>
    </div>
    <div class="info-card">
      <div class="info-label">경도 (Longitude)</div>
      <div class="info-value" id="lngValue">-</div>
    </div>
    <div class="info-card">
      <div class="info-label">속도 (km/h)</div>
      <div class="info-value" id="speedValue">-</div>
    </div>
    <div class="info-card">
      <div class="info-label">배터리 (%)</div>
      <div class="info-value" id="batteryValue">-</div>
      <div class="battery-bar">
        <div class="battery-fill" id="batteryBar" style="width: 0%"></div>
      </div>
    </div>
  </div>

  <script src="app.js"></script>
</body>
</html>
```

### JavaScript (app.js)
```javascript
let map;
let droneMarker;
let pathLine;
let pathCoordinates = [];
let stompClient = null;
let currentSubscription = null;

const WS_URL = 'https://drone-server.p-e.kr/ws';  // HTTPS 사용

// 페이지 로드 시 지도 초기화
window.onload = function() {
  initMap();
};

// 지도 초기화
function initMap() {
  const container = document.getElementById('map');
  const options = {
    center: new kakao.maps.LatLng(37.4979, 127.0276),
    level: 3
  };

  map = new kakao.maps.Map(container, options);

  // 드론 마커 생성
  const markerPosition = new kakao.maps.LatLng(37.4979, 127.0276);
  droneMarker = new kakao.maps.Marker({
    position: markerPosition,
    map: map
  });

  // 경로 선 초기화
  pathLine = new kakao.maps.Polyline({
    path: pathCoordinates,
    strokeWeight: 3,
    strokeColor: '#0066ff',
    strokeOpacity: 0.7,
    strokeStyle: 'solid'
  });
  pathLine.setMap(map);
}

// WebSocket 연결
function connectWebSocket() {
  const routeId = document.getElementById('routeIdInput').value;
  if (!routeId) {
    alert('Route ID를 입력하세요');
    return;
  }

  const socket = new SockJS(WS_URL);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function(frame) {
    console.log('WebSocket 연결 성공!');

    // 토픽 구독
    currentSubscription = stompClient.subscribe('/topic/route/' + routeId, function(message) {
      const data = JSON.parse(message.body);
      handleDronePosition(data);
    });

    console.log(`Route ${routeId} 구독 시작`);
  }, function(error) {
    console.error('WebSocket 연결 실패:', error);
  });
}

// WebSocket 연결 해제
function disconnectWebSocket() {
  if (stompClient && stompClient.connected) {
    if (currentSubscription) {
      currentSubscription.unsubscribe();
    }
    stompClient.disconnect(function() {
      console.log('WebSocket 연결 해제됨');
    });
  }
}

// 드론 위치 데이터 처리
function handleDronePosition(data) {
  console.log('드론 위치 업데이트:', data);

  // UI 업데이트
  document.getElementById('latValue').textContent = data.lat.toFixed(6);
  document.getElementById('lngValue').textContent = data.lng.toFixed(6);
  document.getElementById('speedValue').textContent = data.speed.toFixed(1);
  document.getElementById('batteryValue').textContent = data.battery.toFixed(1);

  // 배터리 바 업데이트
  const batteryBar = document.getElementById('batteryBar');
  batteryBar.style.width = data.battery + '%';

  if (data.battery > 50) {
    batteryBar.className = 'battery-fill';
  } else if (data.battery > 20) {
    batteryBar.className = 'battery-fill warning';
  } else {
    batteryBar.className = 'battery-fill danger';
  }

  // 지도 업데이트
  updateMapMarker(data.lat, data.lng);

  // 경로에 추가
  addToPath(data.lat, data.lng);
}

// 지도 마커 위치 업데이트
function updateMapMarker(lat, lng) {
  smoothMoveMarker(lat, lng);
  map.panTo(new kakao.maps.LatLng(lat, lng));
}

// 부드러운 마커 이동 애니메이션
function smoothMoveMarker(lat, lng, duration = 1500) {
  const startPosition = droneMarker.getPosition();
  const endPosition = new kakao.maps.LatLng(lat, lng);
  const startTime = Date.now();

  function animate() {
    const elapsed = Date.now() - startTime;
    const progress = Math.min(elapsed / duration, 1);

    const currentLat = startPosition.getLat() + (endPosition.getLat() - startPosition.getLat()) * progress;
    const currentLng = startPosition.getLng() + (endPosition.getLng() - startPosition.getLng()) * progress;

    droneMarker.setPosition(new kakao.maps.LatLng(currentLat, currentLng));

    if (progress < 1) {
      requestAnimationFrame(animate);
    }
  }

  animate();
}

// 경로에 추가
function addToPath(lat, lng) {
  pathCoordinates.push(new kakao.maps.LatLng(lat, lng));
  pathLine.setPath(pathCoordinates);
}

// 페이지 언로드 시 연결 해제
window.addEventListener('beforeunload', function() {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect();
  }
});
```

---

## 주의사항 및 팁

### 1. CORS 설정 (로컬 개발)

**현재 백엔드 설정 (WebSocketConfig.java:29):**
```java
.setAllowedOrigins("http://localhost:3000", "http://localhost:5173")
```

**로컬 개발 환경 - 추가 설정 없이 바로 사용 가능 ✅**
- `http://localhost:3000` - React 개발 서버 (npm start)
- `http://localhost:5173` - Vite 개발 서버 (npm run dev)

**다른 포트 사용 시:**
다른 포트를 사용하는 경우에만 백엔드 팀에 요청하여 포트를 추가해야 합니다.

**프론트엔드 배포 시:**
프론트엔드를 배포할 계획이 있다면, 배포 URL을 백엔드 팀에 전달하여 CORS 목록에 추가를 요청하세요.

### 2. WebSocket 재연결 처리
네트워크 연결이 불안정한 경우를 대비해 재연결 로직을 추가하는 것이 좋습니다:

```javascript
function connectWithRetry(routeId, retryCount = 0) {
  const socket = new SockJS(WS_URL);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function(frame) {
    console.log('WebSocket 연결 성공!');
    currentSubscription = stompClient.subscribe('/topic/route/' + routeId, function(message) {
      const data = JSON.parse(message.body);
      handleDronePosition(data);
    });
  }, function(error) {
    console.error('WebSocket 연결 실패:', error);

    // 최대 5번까지 재시도
    if (retryCount < 5) {
      setTimeout(() => {
        console.log(`재연결 시도 ${retryCount + 1}/5...`);
        connectWithRetry(routeId, retryCount + 1);
      }, 3000);  // 3초 후 재시도
    }
  });
}
```

### 3. 애니메이션 duration 조정
백엔드는 2초마다 위치를 전송합니다. 애니메이션 duration을 1500ms로 설정하면 부드러운 이동이 가능합니다.

```javascript
smoothMoveMarker(lat, lng, 1500);  // 1.5초
```

### 4. 배터리 경고 처리
배터리가 20% 이하일 때 사용자에게 경고를 표시할 수 있습니다:

```javascript
function handleDronePosition(data) {
  // ... 기존 코드 ...

  if (data.battery <= 20) {
    alert('드론 배터리가 부족합니다! (' + data.battery.toFixed(1) + '%)');
  }
}
```

### 5. 카카오맵 API 키 발급
카카오맵을 사용하려면 API 키가 필요합니다:
1. https://developers.kakao.com/ 접속
2. 내 애플리케이션 > 애플리케이션 추가하기
3. 앱 키 > JavaScript 키 복사
4. HTML에서 `YOUR_APP_KEY`를 실제 키로 교체

```html
<script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_ACTUAL_API_KEY"></script>
```

### 6. React에서 사용하는 경우

```bash
npm install sockjs-client stompjs
```

```jsx
import React, { useEffect, useState, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

function DroneTracking({ routeId }) {
  const [position, setPosition] = useState({ lat: 0, lng: 0, battery: 0, speed: 0 });
  const stompClientRef = useRef(null);
  const mapRef = useRef(null);
  const markerRef = useRef(null);

  useEffect(() => {
    // 카카오맵 초기화
    const container = document.getElementById('map');
    const options = {
      center: new kakao.maps.LatLng(37.4979, 127.0276),
      level: 3
    };
    mapRef.current = new kakao.maps.Map(container, options);
    markerRef.current = new kakao.maps.Marker({
      position: new kakao.maps.LatLng(37.4979, 127.0276),
      map: mapRef.current
    });

    // WebSocket 연결
    const socket = new SockJS('https://drone-server.p-e.kr/ws');  // HTTPS 사용
    stompClientRef.current = Stomp.over(socket);

    stompClientRef.current.connect({}, (frame) => {
      console.log('Connected:', frame);

      stompClientRef.current.subscribe(`/topic/route/${routeId}`, (message) => {
        const data = JSON.parse(message.body);
        setPosition(data);

        // 마커 위치 업데이트
        const newPosition = new kakao.maps.LatLng(data.lat, data.lng);
        markerRef.current.setPosition(newPosition);
        mapRef.current.panTo(newPosition);
      });
    });

    // 컴포넌트 언마운트 시 연결 해제
    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.disconnect();
      }
    };
  }, [routeId]);

  return (
    <div>
      <div id="map" style={{ width: '100%', height: '600px' }}></div>
      <div>
        <p>위도: {position.lat.toFixed(6)}</p>
        <p>경도: {position.lng.toFixed(6)}</p>
        <p>속도: {position.speed.toFixed(1)} km/h</p>
        <p>배터리: {position.battery.toFixed(1)}%</p>
      </div>
    </div>
  );
}

export default DroneTracking;
```

### 7. 배송 완료 알림
드론이 각 배송지에 도착하면 주문 완료 알림도 전송됩니다:

```javascript
// 주문별 배송 완료 알림 구독
stompClient.subscribe('/topic/order/' + orderId, function(message) {
  const data = JSON.parse(message.body);
  if (data.status === 'FULFILLED') {
    alert('배송이 완료되었습니다!');
    console.log('완료 시간:', data.completedAt);
  }
});
```

**전송 데이터:**
```json
{
  "orderId": 1,
  "status": "FULFILLED",
  "message": "배송이 완료되었습니다!",
  "completedAt": "2025-01-15T12:45:30"
}
```

---

## 참고 자료

- [카카오맵 API 가이드](https://apis.map.kakao.com/web/guide/)
- [STOMP 프로토콜 문서](https://stomp.github.io/)
- [SockJS 공식 문서](https://github.com/sockjs/sockjs-client)
- 프로젝트 예시 파일: `client-examples/drone-tracking.html`
- 지도 통합 가이드: `client-examples/MAP_INTEGRATION_GUIDE.md`

