# 지도 API 통합 가이드

드론 실시간 위치 추적을 위한 지도 API 통합 방법입니다.

## 1. Google Maps API 통합

### 설정:
```html
<script src="https://maps.googleapis.com/maps/api/js?key=YOUR_API_KEY"></script>
```

### 초기화:
```javascript
let map;
let droneMarker;

function initMap() {
    // 지도 생성 (초기 중심: 서울)
    map = new google.maps.Map(document.getElementById('map'), {
        center: { lat: 37.5665, lng: 126.9780 },
        zoom: 13
    });

    // 드론 마커 생성
    droneMarker = new google.maps.Marker({
        position: { lat: 37.5665, lng: 126.9780 },
        map: map,
        title: '드론',
        icon: {
            url: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png',
            scaledSize: new google.maps.Size(50, 50)
        }
    });
}
```

### 위치 업데이트:
```javascript
function updateMapMarker(lat, lng) {
    const position = { lat: lat, lng: lng };

    // 마커 위치 업데이트
    droneMarker.setPosition(position);

    // 지도 중심 이동 (선택사항)
    map.panTo(position);
}
```

---

## 2. Kakao Maps API 통합

### 설정:
```html
<script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_APP_KEY"></script>
```

### 초기화:
```javascript
let map;
let droneMarker;

function initMap() {
    const container = document.getElementById('map');
    const options = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 3
    };

    map = new kakao.maps.Map(container, options);

    // 드론 마커 생성
    const markerPosition = new kakao.maps.LatLng(37.5665, 126.9780);
    droneMarker = new kakao.maps.Marker({
        position: markerPosition,
        map: map
    });

    // 커스텀 아이콘 (선택사항)
    const imageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png';
    const imageSize = new kakao.maps.Size(64, 69);
    const imageOption = { offset: new kakao.maps.Point(27, 69) };
    const markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);
    droneMarker.setImage(markerImage);
}
```

### 위치 업데이트:
```javascript
function updateMapMarker(lat, lng) {
    const position = new kakao.maps.LatLng(lat, lng);

    // 마커 위치 업데이트
    droneMarker.setPosition(position);

    // 지도 중심 이동 (선택사항)
    map.panTo(position);
}
```

---

## 3. Naver Maps API 통합

### 설정:
```html
<script src="https://openapi.map.naver.com/openapi/v3/maps.js?ncpClientId=YOUR_CLIENT_ID"></script>
```

### 초기화:
```javascript
let map;
let droneMarker;

function initMap() {
    map = new naver.maps.Map('map', {
        center: new naver.maps.LatLng(37.5665, 126.9780),
        zoom: 13
    });

    // 드론 마커 생성
    droneMarker = new naver.maps.Marker({
        position: new naver.maps.LatLng(37.5665, 126.9780),
        map: map,
        icon: {
            content: '<div style="background: #0066ff; width: 30px; height: 30px; border-radius: 50%; border: 3px solid white;"></div>',
            size: new naver.maps.Size(30, 30),
            anchor: new naver.maps.Point(15, 15)
        }
    });
}
```

### 위치 업데이트:
```javascript
function updateMapMarker(lat, lng) {
    const position = new naver.maps.LatLng(lat, lng);

    // 마커 위치 업데이트
    droneMarker.setPosition(position);

    // 지도 중심 이동 (선택사항)
    map.panTo(position);
}
```

---

## 4. 부드러운 마커 이동 애니메이션

### Google Maps:
```javascript
function smoothMoveMarker(lat, lng, duration = 1000) {
    const startPosition = droneMarker.getPosition();
    const endPosition = { lat: lat, lng: lng };
    const startTime = Date.now();

    function animate() {
        const elapsed = Date.now() - startTime;
        const progress = Math.min(elapsed / duration, 1);

        const currentLat = startPosition.lat() + (endPosition.lat - startPosition.lat()) * progress;
        const currentLng = startPosition.lng() + (endPosition.lng - startPosition.lng()) * progress;

        droneMarker.setPosition({ lat: currentLat, lng: currentLng });

        if (progress < 1) {
            requestAnimationFrame(animate);
        }
    }

    animate();
}
```

### Kakao Maps:
```javascript
function smoothMoveMarker(lat, lng, duration = 1000) {
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
```

---

## 5. 경로 선 그리기

드론이 이동한 경로를 선으로 표시:

### Google Maps:
```javascript
let pathCoordinates = [];
let pathLine;

function initPathLine() {
    pathLine = new google.maps.Polyline({
        path: pathCoordinates,
        geodesic: true,
        strokeColor: '#FF0000',
        strokeOpacity: 1.0,
        strokeWeight: 2,
        map: map
    });
}

function addToPath(lat, lng) {
    pathCoordinates.push({ lat: lat, lng: lng });
    pathLine.setPath(pathCoordinates);
}
```

### Kakao Maps:
```javascript
let pathCoordinates = [];
let pathLine;

function initPathLine() {
    pathLine = new kakao.maps.Polyline({
        path: pathCoordinates,
        strokeWeight: 3,
        strokeColor: '#FF0000',
        strokeOpacity: 0.7,
        strokeStyle: 'solid'
    });
    pathLine.setMap(map);
}

function addToPath(lat, lng) {
    pathCoordinates.push(new kakao.maps.LatLng(lat, lng));
    pathLine.setPath(pathCoordinates);
}
```

---

## 6. 완전한 통합 예시 (Kakao Maps)

```javascript
let map;
let droneMarker;
let pathCoordinates = [];
let pathLine;

// 페이지 로드 시 지도 초기화
window.onload = function() {
    initMap();
};

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

// WebSocket에서 호출
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

    // 로그 추가
    addLog(`위치 업데이트: (${data.lat.toFixed(6)}, ${data.lng.toFixed(6)}) | 배터리: ${data.battery.toFixed(1)}%`);
}

function updateMapMarker(lat, lng) {
    const position = new kakao.maps.LatLng(lat, lng);

    // 마커 위치 업데이트 (부드러운 애니메이션)
    smoothMoveMarker(lat, lng);

    // 지도 중심 이동
    map.panTo(position);
}

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

function addToPath(lat, lng) {
    pathCoordinates.push(new kakao.maps.LatLng(lat, lng));
    pathLine.setPath(pathCoordinates);
}
```

---

## 7. 사용 방법

1. **API 키 발급**: 선택한 지도 서비스에서 API 키 발급
   - Google Maps: https://console.cloud.google.com/
   - Kakao Maps: https://developers.kakao.com/
   - Naver Maps: https://www.ncloud.com/product/applicationService/maps

2. **HTML 파일에 스크립트 추가**: 발급받은 API 키로 지도 라이브러리 로드

3. **초기화 코드 추가**: `initMap()` 함수를 페이지 로드 시 실행

4. **WebSocket 핸들러에서 호출**: `handleDronePosition()` 함수에서 `updateMapMarker()` 호출

5. **테스트**:
   ```bash
   # 서버 실행
   ./gradlew bootRun

   # 브라우저에서 HTML 파일 열기
   # WebSocket 연결 -> 배송 시작 -> 드론 이동 확인
   ```

---

## 8. 주의사항

- **API 키 보안**: 프로덕션 환경에서는 API 키를 환경 변수로 관리
- **업데이트 주기**: 서버는 2초마다 위치 전송, 클라이언트는 부드러운 애니메이션으로 처리
- **CORS 설정**: WebSocketConfig에서 클라이언트 도메인 허용 필요 (현재: localhost:3000, localhost:5173)
- **배터리 알림**: 배터리가 20% 이하일 때 경고 표시
- **연결 끊김 처리**: WebSocket 연결이 끊기면 자동 재연결 로직 추가 권장
