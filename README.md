# Community (Backend)
- 커뮤니티 웹 애플리케이션 서비스의 Spring Boot 기반 REST API 서버

## 기술 스택
- Java 21, Gradle
- Spring Boot 3.5.6: Web, Validation, Data JPA, Security
- Lombok, Springdoc OpenAPI UI
- MySQL, P6Spy

## 주요 기능
- 회원가입/로그인/로그아웃, 프로필/비밀번호 수정
- 게시글 목록/상세 조회 및 작성/수정/삭제, 페이지네이션
- 게시글별 댓글 CRUD와 좋아요 토글, 집계 카운트 반영
- 공통 API 응답 포맷(`ApiResponse`)과 코드 기반 상태 동기화
- Spring Security Custom Filter를 통한 인증/인가 처리

## 시연영상/스크린샷
- 추가 예정


## 프로젝트 구조 / 폴더 설명
```
ktb3-theta-full-community-be/
 ㄴ src/main/java/ktb/week4/community/
    ㄴ domain/
       ㄴ article, comment, like, user  # 컨트롤러/서비스/리포지토리/엔티티
    ㄴ config, security, global        # 설정(CORS/Security), JWT, 공통 예외/응답
 ㄴ src/main/resources/
    ㄴ application.yml                 # DB/로그/업로드 경로 등 설정
    ㄴ templates, static               # 필요 시 뷰/정적 리소스
```

## 학습한 내용
- 추가 예정

## 설치 및 실행 방법
1) 저장소 클론: `git clone <repo-url>`
2) 백엔드 디렉터리 이동: `cd ktb3-theta-full-community-be`
3) 환경 준비: JDK 21, MySQL 데이터베이스 `ktb_community` 생성 후 `src/main/resources/application.yml`의 접속 정보 확인/수정
4) 애플리케이션 실행: `./gradlew bootRun`
5) 선택 실행: 테스트 `./gradlew test`, 문서(UI) `http://localhost:8080/swagger-ui.html` 확인
