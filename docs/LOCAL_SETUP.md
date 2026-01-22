# 로컬 개발 환경 설정
## 실행 방법

```bash
# 1. 저장소 클론
git clone https://github.com/your-repo/AIBE4_Project2_Team5_Connect5.git

# 2. 디렉토리 이동
cd AIBE4_Project2_Team5_Connect5

# 3. Docker 컨테이너 실행 (PostgreSQL, Redis)
docker compose up -d

# 4. 컨테이너 실행 확인
docker ps

# 5. 애플리케이션 실행
./gradlew bootRun
```

## 접속 정보

| 서비스 | URL |
|--------|-----|
| 애플리케이션 | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/api-docs |
