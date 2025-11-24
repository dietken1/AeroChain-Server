# ✅ 해야할 일 체크리스트

## 🚨 필수 작업 (반드시 해야함)

### 1. 환경변수 설정
- [ ] `.env.example` 파일을 복사하여 `.env` 파일 생성
  ```bash
  cp .env.example .env
  ```
- [ ] `.env` 파일에서 **DB_PASSWORD를 본인의 MySQL 비밀번호로 변경**
  ```
  DB_PASSWORD=a9153243  # 이 부분을 본인 비밀번호로 수정
  ```
- [ ] `.gitignore`에 `.env`가 포함되어 있는지 확인 (이미 추가됨 ✅)

### 2. MySQL 데이터베이스 확인
- [ ] MySQL이 실행 중인지 확인
  ```bash
  # Windows: 서비스에서 MySQL 확인
  # Mac: brew services list
  # Linux: sudo systemctl status mysql
  ```
- [ ] `drone_delivery` 데이터베이스가 존재하는지 확인
  ```sql
  mysql -u root -p
  SHOW DATABASES;
  -- drone_delivery가 없으면:
  -- CREATE DATABASE drone_delivery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```

### 3. 애플리케이션 실행 테스트
- [ ] 로컬에서 애플리케이션 실행
  ```bash
  ./gradlew bootRun
  ```
- [ ] Swagger UI 접속 확인: http://localhost:8080/swagger-ui.html
- [ ] API 테스트: GET /api/stores 등 호출해보기

---

## 🐳 Docker 사용 시 (선택사항)

### 4. Docker 설정 (Docker를 사용하려면)
- [ ] Docker Desktop 설치 확인
- [ ] Docker Compose로 실행 테스트
  ```bash
  docker-compose up -d
  docker-compose logs -f
  ```
- [ ] 접속 확인: http://localhost:8080/swagger-ui.html
- [ ] 정리: `docker-compose down`

---

## 🔧 GitHub Actions CI/CD 설정 (선택사항)

### 5. GitHub 저장소 설정
- [ ] GitHub에 새 저장소 생성 (비공개 추천)
- [ ] 로컬 저장소를 GitHub에 연결
  ```bash
  git remote add origin https://github.com/<username>/<repository>.git
  git branch -M main
  git push -u origin main
  ```

### 6. GitHub Actions 권한 설정
- [ ] Settings > Actions > General
- [ ] "Allow all actions and reusable workflows" 선택
- [ ] Workflow permissions에서 "Read and write permissions" 선택
- [ ] Save 클릭

### 7. CI/CD 동작 확인
- [ ] 코드 푸시 후 Actions 탭에서 CI 실행 확인
  ```bash
  git add .
  git commit -m "test: CI 테스트"
  git push origin main
  ```
- [ ] Actions 탭에서 빌드 성공 확인

### 8. Docker 이미지 배포 테스트 (선택)
- [ ] main 브랜치에 푸시하여 Docker 이미지 자동 빌드 확인
- [ ] 또는 릴리스 태그 생성
  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```

---

## 🗺️ WebSocket & 지도 통합 (선택사항)

### 9. WebSocket 테스트
- [ ] 서버 실행 상태에서 `client-examples/drone-tracking.html` 열기
- [ ] Route ID "1" 입력 후 "연결" 버튼 클릭
- [ ] "배송 시작" 버튼 클릭
- [ ] 드론 위치가 2초마다 업데이트되는지 확인

### 10. 지도 API 통합 (프론트엔드 개발 시)
- [ ] 사용할 지도 API 선택 (Google Maps, Kakao Maps, Naver Maps)
- [ ] API 키 발급
  - Google Maps: https://console.cloud.google.com/
  - Kakao Maps: https://developers.kakao.com/
  - Naver Maps: https://www.ncloud.com/product/applicationService/maps
- [ ] `client-examples/MAP_INTEGRATION_GUIDE.md` 참고하여 통합
- [ ] 지도에서 드론 마커가 부드럽게 이동하는지 확인

---

## 🚀 프로덕션 배포 (실제 서비스 운영 시)

### 11. 프로덕션 환경 설정
- [ ] `application-prod.yml` 파일 생성 (SETUP_GUIDE.md 참고)
- [ ] 프로덕션 DB 설정 (AWS RDS, Azure Database 등)
- [ ] 환경변수를 프로덕션 값으로 설정
- [ ] CORS 설정에 프로덕션 도메인 추가 (WebSocketConfig.java)

### 12. 보안 설정
- [ ] DB 비밀번호를 강력한 비밀번호로 변경
- [ ] SSL/TLS 인증서 설정 (Let's Encrypt 등)
- [ ] 방화벽 설정 (8080 포트만 오픈)
- [ ] Actuator 엔드포인트 보안 설정
- [ ] GitHub Secrets에 민감한 정보 저장

### 13. 모니터링 & 로깅
- [ ] 로그 레벨을 INFO 또는 WARN으로 변경
- [ ] 모니터링 도구 설정 (Prometheus, Grafana 등)
- [ ] 에러 추적 도구 설정 (Sentry 등)
- [ ] Health Check 엔드포인트 확인

---

## 📋 현재 상태 확인

### 체크포인트 1: 로컬 개발 환경
- [ ] MySQL 연결 성공
- [ ] 애플리케이션 실행 성공
- [ ] Swagger UI 접속 가능
- [ ] API 호출 성공

### 체크포인트 2: Docker 환경
- [ ] Docker Compose 실행 성공
- [ ] 컨테이너 정상 동작
- [ ] API 호출 성공

### 체크포인트 3: CI/CD
- [ ] GitHub Actions CI 성공
- [ ] GitHub Actions CD 성공 (Docker 이미지 빌드)
- [ ] 배포된 이미지 Pull 가능

### 체크포인트 4: WebSocket
- [ ] WebSocket 연결 성공
- [ ] 드론 위치 실시간 업데이트 확인
- [ ] 배터리 정보 정상 표시

---

## 🎯 우선순위

### 🔴 높음 (지금 바로)
1. 환경변수 설정 (.env 파일)
2. MySQL 데이터베이스 확인
3. 애플리케이션 실행 테스트

### 🟡 중간 (곧)
4. Docker 실행 테스트
5. WebSocket 테스트
6. GitHub Actions 설정

### 🟢 낮음 (나중에)
7. 지도 API 통합
8. 프로덕션 배포 설정
9. 모니터링 설정

---

## 💡 팁

- **시작이 어렵다면**: Docker Compose 사용 (`docker-compose up -d`)
- **빠른 테스트**: Swagger UI에서 API 직접 호출
- **WebSocket 디버깅**: 브라우저 개발자 도구 > Network > WS 탭
- **문제 발생 시**: SETUP_GUIDE.md의 트러블슈팅 섹션 참고

---

## 📞 도움이 필요하면

- [SETUP_GUIDE.md](SETUP_GUIDE.md) - 상세 설정 가이드
- [API_SPECIFICATION.md](API_SPECIFICATION.md) - API 명세서
- [.github/workflows/README.md](.github/workflows/README.md) - CI/CD 가이드
- [client-examples/MAP_INTEGRATION_GUIDE.md](client-examples/MAP_INTEGRATION_GUIDE.md) - 지도 통합 가이드
