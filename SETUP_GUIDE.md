# 🚀 프로젝트 설정 가이드

이 가이드는 드론 배송 시스템을 설정하고 실행하는 방법을 단계별로 설명합니다.

---

## 📋 목차

1. [로컬 개발 환경 설정](#1-로컬-개발-환경-설정)
2. [Docker로 실행](#2-docker로-실행)
3. [GitHub Actions CI/CD 설정](#3-github-actions-cicd-설정)
4. [WebSocket 테스트](#4-websocket-테스트)
5. [프로덕션 배포](#5-프로덕션-배포)

---

## 1. 로컬 개발 환경 설정

### 1.1 필수 소프트웨어 설치

- ✅ **JDK 17** 이상
- ✅ **MySQL 8.0** 이상
- ✅ **Git**
- 🔧 **IntelliJ IDEA** 또는 Eclipse (권장)

### 1.2 MySQL 데이터베이스 생성

```sql
-- MySQL에 로그인
mysql -u root -p

-- 데이터베이스 생성
CREATE DATABASE drone_delivery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 (선택사항)
CREATE USER 'drone_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON drone_delivery.* TO 'drone_user'@'localhost';
FLUSH PRIVILEGES;
```

### 1.3 환경변수 설정

#### 방법 1: .env 파일 사용 (권장)

```bash
# .env.example을 복사하여 .env 파일 생성
cp .env.example .env

# .env 파일 편집
# Windows
notepad .env

# Mac/Linux
nano .env
```

**.env 파일 내용:**
```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=drone_delivery
DB_USERNAME=root
DB_PASSWORD=a9153243
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8080
SPRING_JPA_DDL_AUTO=none
SPRING_JPA_SHOW_SQL=true
LOG_LEVEL=debug
```

#### 방법 2: IDE에서 환경변수 설정

**IntelliJ IDEA:**
1. Run > Edit Configurations
2. Environment variables에 추가:
   ```
   DB_HOST=localhost;DB_PORT=3306;DB_NAME=drone_delivery;DB_USERNAME=root;DB_PASSWORD=your_password
   ```

**Eclipse:**
1. Run > Run Configurations
2. Environment 탭에서 환경변수 추가

#### 방법 3: 시스템 환경변수 설정

**Windows:**
```cmd
setx DB_HOST "localhost"
setx DB_PORT "3306"
setx DB_NAME "drone_delivery"
setx DB_USERNAME "root"
setx DB_PASSWORD "your_password"
```

**Mac/Linux:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=drone_delivery
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

### 1.4 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew clean build
java -jar build/libs/Database-Project-Server-0.0.1-SNAPSHOT.jar
```

### 1.5 실행 확인

- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health (Actuator 활성화 시)

---

## 2. Docker로 실행

Docker를 사용하면 **MySQL과 애플리케이션을 한 번에 실행**할 수 있습니다.

### 2.1 필수 소프트웨어 설치

- ✅ **Docker Desktop** (Windows/Mac) 또는 **Docker Engine** (Linux)
- ✅ **Docker Compose**

다운로드: https://www.docker.com/products/docker-desktop/

### 2.2 Docker Compose로 전체 스택 실행

```bash
# 백그라운드에서 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 특정 서비스만 로그 확인
docker-compose logs -f app

# 실행 중인 컨테이너 확인
docker-compose ps

# 중지
docker-compose down

# 볼륨까지 삭제 (데이터베이스 초기화)
docker-compose down -v
```

### 2.3 Docker만 사용하여 실행

```bash
# MySQL 컨테이너 실행
docker run -d \
  --name drone-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=drone_delivery \
  -e MYSQL_USER=drone_user \
  -e MYSQL_PASSWORD=drone_password \
  -p 3306:3306 \
  mysql:8.0

# 애플리케이션 이미지 빌드
docker build -t drone-delivery-app .

# 애플리케이션 컨테이너 실행
docker run -d \
  --name drone-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/drone_delivery \
  -e SPRING_DATASOURCE_USERNAME=drone_user \
  -e SPRING_DATASOURCE_PASSWORD=drone_password \
  drone-delivery-app
```

### 2.4 실행 확인

```bash
# 컨테이너 상태 확인
docker ps

# 애플리케이션 로그 확인
docker logs drone-app -f

# MySQL 접속 테스트
docker exec -it drone-mysql mysql -u drone_user -pdrone_password drone_delivery
```

---

## 3. GitHub Actions CI/CD 설정

### 3.1 GitHub 저장소 생성

```bash
# 로컬 저장소를 GitHub에 푸시
git remote add origin https://github.com/<username>/<repository>.git
git branch -M main
git push -u origin main
```

### 3.2 GitHub Actions 활성화

1. GitHub 저장소 > **Settings**
2. **Actions** > General
3. **Allow all actions and reusable workflows** 선택

### 3.3 Workflow Permissions 설정

1. GitHub 저장소 > **Settings**
2. **Actions** > General
3. **Workflow permissions**에서 **Read and write permissions** 선택
4. **Save** 클릭

### 3.4 Secrets 설정 (선택사항)

민감한 정보를 GitHub Secrets에 저장:

1. GitHub 저장소 > **Settings**
2. **Secrets and variables** > **Actions**
3. **New repository secret** 클릭

**추가할 Secrets:**

| Secret Name | 설명 | 예시 |
|-------------|------|------|
| `DATABASE_PASSWORD` | 프로덕션 DB 비밀번호 | `your_secure_password` |
| `SLACK_WEBHOOK_URL` | Slack 알림용 (선택) | `https://hooks.slack.com/...` |
| `DEPLOY_SSH_KEY` | 서버 배포용 SSH 키 (선택) | `-----BEGIN RSA PRIVATE KEY-----...` |

### 3.5 CI/CD 자동 실행 확인

```bash
# 코드 푸시하면 CI가 자동 실행됨
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main

# GitHub > Actions 탭에서 실행 상태 확인
```

### 3.6 Docker 이미지 배포

#### 방법 1: main 브랜치 푸시
```bash
git push origin main
# → Docker 이미지가 ghcr.io/<username>/<repository>:main으로 푸시됨
```

#### 방법 2: 릴리스 태그 생성
```bash
git tag v1.0.0
git push origin v1.0.0
# → Docker 이미지가 ghcr.io/<username>/<repository>:v1.0.0으로 푸시됨
```

### 3.7 배포된 Docker 이미지 사용

```bash
# GitHub Container Registry 로그인
echo $GITHUB_TOKEN | docker login ghcr.io -u <username> --password-stdin

# 이미지 pull
docker pull ghcr.io/<username>/<repository>:main

# 실행
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/drone_delivery \
  -e SPRING_DATASOURCE_USERNAME=drone_user \
  -e SPRING_DATASOURCE_PASSWORD=drone_password \
  ghcr.io/<username>/<repository>:main
```

---

## 4. WebSocket 테스트

### 4.1 HTML 클라이언트 사용

```bash
# 브라우저에서 HTML 파일 열기
# Windows
start client-examples/drone-tracking.html

# Mac
open client-examples/drone-tracking.html

# Linux
xdg-open client-examples/drone-tracking.html
```

### 4.2 WebSocket 연결 테스트

1. **서버 실행 확인**: http://localhost:8080
2. **HTML 클라이언트 열기**: `client-examples/drone-tracking.html`
3. **Route ID 입력**: 1
4. **"연결" 버튼 클릭**
5. **"배송 시작" 버튼 클릭**
6. 드론 위치가 2초마다 자동 업데이트되는지 확인

### 4.3 지도 API 통합 (선택사항)

`client-examples/MAP_INTEGRATION_GUIDE.md` 참고:

- Google Maps API
- Kakao Maps API
- Naver Maps API

---

## 5. 프로덕션 배포

### 5.1 환경변수 설정 (프로덕션)

**.env 파일 (프로덕션 서버):**
```properties
DB_HOST=your-rds-endpoint.amazonaws.com
DB_PORT=3306
DB_NAME=drone_delivery
DB_USERNAME=admin
DB_PASSWORD=your_secure_password
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
SPRING_JPA_DDL_AUTO=none
SPRING_JPA_SHOW_SQL=false
LOG_LEVEL=info
```

### 5.2 application-prod.yml 생성

`src/main/resources/application-prod.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        show_sql: false
        format_sql: false

logging:
  level:
    root: INFO
    backend.databaseproject: INFO
    org.hibernate: WARN

server:
  error:
    include-message: never
    include-stacktrace: never
```

### 5.3 프로덕션 배포 체크리스트

- [ ] **데이터베이스 백업** 완료
- [ ] **환경변수** 프로덕션 값으로 설정
- [ ] **CORS 설정** 프로덕션 도메인 추가 (WebSocketConfig.java)
- [ ] **ddl-auto** `none` 또는 `validate`로 설정
- [ ] **show_sql** `false`로 설정
- [ ] **로그 레벨** `INFO` 또는 `WARN`으로 설정
- [ ] **Actuator 엔드포인트** 보안 설정
- [ ] **SSL/TLS** 인증서 설정 (HTTPS)
- [ ] **방화벽** 포트 8080 오픈
- [ ] **Health Check** 엔드포인트 동작 확인
- [ ] **모니터링** 설정 (Prometheus, Grafana 등)

### 5.4 AWS EC2 배포 예시

```bash
# EC2 인스턴스 접속
ssh -i your-key.pem ubuntu@your-ec2-ip

# Docker 설치
sudo apt update
sudo apt install docker.io docker-compose -y
sudo usermod -aG docker ubuntu

# 프로젝트 클론
git clone https://github.com/<username>/<repository>.git
cd <repository>

# 환경변수 설정
nano .env

# Docker Compose로 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f
```

### 5.5 AWS RDS 사용 시

```bash
# RDS 엔드포인트를 환경변수로 설정
export DB_HOST=your-rds-endpoint.rds.amazonaws.com
export DB_USERNAME=admin
export DB_PASSWORD=your_secure_password

# 애플리케이션 실행
java -jar app.jar
```

---

## 🛠️ 트러블슈팅

### 문제 1: "Cannot connect to database"

**해결:**
```bash
# MySQL이 실행 중인지 확인
# Windows
services.msc → MySQL 서비스 확인

# Mac
brew services list

# Linux
sudo systemctl status mysql

# Docker
docker ps | grep mysql
```

### 문제 2: "Port 8080 already in use"

**해결:**
```bash
# 포트를 사용 중인 프로세스 찾기
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :8080
kill -9 <PID>

# 또는 다른 포트 사용
export SERVER_PORT=8081
./gradlew bootRun
```

### 문제 3: Gradle 빌드 실패

**해결:**
```bash
# Gradle 캐시 삭제
./gradlew clean

# Gradle wrapper 재다운로드
./gradlew wrapper --gradle-version 8.5

# 실행 권한 추가
chmod +x gradlew
```

### 문제 4: Docker 이미지 빌드 실패

**해결:**
```bash
# Docker 캐시 삭제
docker builder prune -a

# 이미지 강제 재빌드
docker-compose build --no-cache

# 로그 확인
docker-compose logs app
```

### 문제 5: WebSocket 연결 실패

**해결:**
1. CORS 설정 확인 (WebSocketConfig.java)
2. 방화벽에서 8080 포트 열기
3. 프록시/로드밸런서 WebSocket 지원 확인
4. 브라우저 콘솔에서 오류 확인

---

## 📚 추가 자료

- [API 명세서](API_SPECIFICATION.md)
- [GitHub Actions 가이드](.github/workflows/README.md)
- [지도 API 통합 가이드](client-examples/MAP_INTEGRATION_GUIDE.md)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Docker 공식 문서](https://docs.docker.com/)

---

## ✅ 빠른 시작 체크리스트

### 최소 설정 (5분):

1. [ ] MySQL 데이터베이스 생성
2. [ ] `.env.example` 복사하여 `.env` 생성
3. [ ] `.env` 파일에 DB 비밀번호 입력
4. [ ] `./gradlew bootRun` 실행
5. [ ] http://localhost:8080/swagger-ui.html 접속

### Docker 사용 (2분):

1. [ ] Docker Desktop 설치 및 실행
2. [ ] `docker-compose up -d` 실행
3. [ ] http://localhost:8080/swagger-ui.html 접속

### GitHub Actions (3분):

1. [ ] GitHub 저장소 생성
2. [ ] 코드 푸시: `git push origin main`
3. [ ] Actions 탭에서 실행 확인

---

완료! 🎉 문제가 있으면 트러블슈팅 섹션을 참고하세요.
