# GitHub Actions CI/CD 파이프라인

이 프로젝트는 GitHub Actions를 사용하여 CI/CD 파이프라인을 구축했습니다.

## 📋 워크플로우 목록

### 1. CI - Build and Test (`ci.yml`)

**트리거 조건:**
- `main`, `develop` 브랜치에 push
- `main`, `develop` 브랜치로 Pull Request

**작업 내용:**
1. ✅ 코드 체크아웃
2. ☕ JDK 17 설치
3. 🔧 Gradle 빌드 (테스트 제외)
4. 🧪 테스트 실행
5. 📊 테스트 결과 발행
6. 📦 빌드 아티팩트 업로드 (JAR 파일, 7일 보관)
7. 📄 테스트 리포트 업로드 (7일 보관)

**실행 시간:** 약 3-5분

---

### 2. CD - Build and Deploy Docker Image (`cd.yml`)

**트리거 조건:**
- `main` 브랜치에 push
- `v*.*.*` 형식의 태그 push (예: v1.0.0)

**작업 내용:**
1. ✅ 코드 체크아웃
2. ☕ JDK 17 설치
3. 🔧 Gradle 빌드
4. 🐳 Docker Buildx 설정
5. 🔑 GitHub Container Registry 로그인
6. 🏷️ Docker 이미지 메타데이터 추출
7. 📦 Docker 이미지 빌드 및 푸시

**이미지 저장소:** `ghcr.io/<username>/<repository>`

**실행 시간:** 약 5-10분

---

## 🚀 사용 방법

### 1. GitHub Actions 활성화

GitHub 저장소 설정에서 Actions가 활성화되어 있는지 확인:
```
Settings > Actions > General > Actions permissions
✅ Allow all actions and reusable workflows
```

### 2. 코드 푸시로 CI 실행

```bash
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main
```

CI 워크플로우가 자동으로 실행되어:
- 빌드 확인
- 테스트 실행
- 결과 리포트 생성

### 3. Pull Request로 CI 실행

```bash
git checkout -b feature/new-feature
git add .
git commit -m "feat: 새로운 기능 개발"
git push origin feature/new-feature
```

GitHub에서 Pull Request 생성 시 CI가 자동 실행되어 변경사항 검증

### 4. Docker 이미지 빌드 및 배포

#### 방법 1: main 브랜치에 푸시
```bash
git checkout main
git merge develop
git push origin main
```

#### 방법 2: 릴리스 태그 생성
```bash
git tag v1.0.0
git push origin v1.0.0
```

CD 워크플로우가 실행되어 Docker 이미지를 자동으로 빌드하고 GitHub Container Registry에 푸시합니다.

### 5. Docker 이미지 사용

#### GitHub Container Registry에서 이미지 pull:
```bash
# 로그인
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# 이미지 pull
docker pull ghcr.io/<username>/<repository>:main

# 실행
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/drone_delivery \
  -e SPRING_DATASOURCE_USERNAME=drone_user \
  -e SPRING_DATASOURCE_PASSWORD=drone_password \
  ghcr.io/<username>/<repository>:main
```

---

## 🐳 로컬 Docker 개발

### Docker Compose로 전체 스택 실행

```bash
# 빌드 및 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 중지
docker-compose down

# 볼륨까지 삭제 (데이터베이스 초기화)
docker-compose down -v
```

**접속:**
- 애플리케이션: http://localhost:8080
- MySQL: localhost:3306 (drone_user / drone_password)
- Swagger UI: http://localhost:8080/swagger-ui.html

### Docker만 사용하여 빌드 및 실행

```bash
# 이미지 빌드
docker build -t drone-delivery-app .

# 실행 (MySQL은 별도 실행 필요)
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/drone_delivery \
  -e SPRING_DATASOURCE_USERNAME=drone_user \
  -e SPRING_DATASOURCE_PASSWORD=drone_password \
  drone-delivery-app
```

---

## 🔧 워크플로우 커스터마이징

### 테스트 건너뛰기 옵션 추가

`ci.yml` 파일에서:

```yaml
- name: Build with Gradle
  run: ./gradlew clean build -x test
  if: contains(github.event.head_commit.message, '[skip tests]')
```

커밋 메시지에 `[skip tests]`를 포함하면 테스트를 건너뜁니다:
```bash
git commit -m "docs: 문서 업데이트 [skip tests]"
```

### Slack 알림 추가

`cd.yml` 파일 끝에 추가:

```yaml
- name: Send Slack notification
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    text: 'Deployment completed!'
    webhook_url: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### 환경별 배포 설정

개발/스테이징/프로덕션 환경별로 다른 설정 사용:

```yaml
- name: Deploy to Development
  if: github.ref == 'refs/heads/develop'
  run: |
    echo "Deploying to dev environment"
    # 개발 서버 배포 스크립트

- name: Deploy to Production
  if: github.ref == 'refs/heads/main'
  run: |
    echo "Deploying to production environment"
    # 프로덕션 서버 배포 스크립트
```

---

## 📊 Actions 결과 확인

### GitHub UI에서 확인

1. 저장소 > Actions 탭
2. 워크플로우 선택 (CI 또는 CD)
3. 특정 실행 선택하여 로그 확인

### 배지(Badge) 추가

README.md에 추가:

```markdown
[![CI](https://github.com/<username>/<repository>/actions/workflows/ci.yml/badge.svg)](https://github.com/<username>/<repository>/actions/workflows/ci.yml)
[![CD](https://github.com/<username>/<repository>/actions/workflows/cd.yml/badge.svg)](https://github.com/<username>/<repository>/actions/workflows/cd.yml)
```

---

## 🔐 Secrets 설정

GitHub 저장소에서 민감한 정보를 관리:

```
Settings > Secrets and variables > Actions > New repository secret
```

**필요한 Secrets:**
- `GITHUB_TOKEN`: 자동으로 제공됨 (Docker 이미지 푸시용)
- `SLACK_WEBHOOK_URL`: Slack 알림용 (선택)
- `DEPLOY_SSH_KEY`: 서버 배포용 SSH 키 (선택)
- `DATABASE_PASSWORD`: 프로덕션 DB 비밀번호 (선택)

**사용 예시:**
```yaml
env:
  DATABASE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
```

---

## 🛠️ 트러블슈팅

### 문제: Gradle 빌드 실패

**해결:**
```bash
# 로컬에서 먼저 빌드 확인
./gradlew clean build

# gradlew 실행 권한 추가
chmod +x gradlew
git add gradlew
git commit -m "fix: gradlew 실행 권한 추가"
```

### 문제: Docker 이미지 푸시 권한 오류

**해결:**
```
Settings > Actions > General > Workflow permissions
✅ Read and write permissions
```

### 문제: 테스트 실패

**해결:**
- Actions 탭에서 테스트 로그 확인
- 로컬에서 `./gradlew test --info` 실행하여 상세 로그 확인
- 테스트 리포트 아티팩트 다운로드하여 상세 분석

### 문제: Docker 빌드 시간이 너무 오래 걸림

**해결:**
- GitHub Actions 캐시 사용 (이미 설정됨)
- 멀티스테이지 빌드 최적화 (이미 설정됨)
- 불필요한 파일 제외 (`.dockerignore` 확인)

---

## 📚 참고 자료

- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [Docker 공식 문서](https://docs.docker.com/)
- [Gradle 빌드 최적화](https://docs.gradle.org/current/userguide/performance.html)
- [Spring Boot Docker 가이드](https://spring.io/guides/gs/spring-boot-docker/)

---

## ✅ 체크리스트

배포 전 확인사항:

- [ ] 모든 테스트가 통과하는지 확인
- [ ] application.yml 설정이 올바른지 확인
- [ ] Docker 이미지가 정상적으로 빌드되는지 확인
- [ ] 환경 변수가 올바르게 설정되었는지 확인
- [ ] 데이터베이스 마이그레이션이 필요한지 확인
- [ ] API 문서가 최신 상태인지 확인
- [ ] CORS 설정이 프로덕션 도메인을 포함하는지 확인
