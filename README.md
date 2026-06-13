# Auth Basic Project

## Overview

- 기본 인증/인가 프로젝트입니다.
- 회원 가입, 로그인, JWT 인증, 소셜 인증, Toss 본인인증, SMS/Email 연동의 기본 구조를 제공합니다.
- `E:\00.source\firsthabit\chalk_auth`의 최신 Spring Boot/Gradle 기준을 기본 프로젝트용 설정으로 정리했습니다.

## 기술 스택

- Java 21
- Spring Boot 4.0.6
- Gradle Wrapper 8.14
- Spring Security + JWT
- Spring Data JPA, QueryDSL 7.1, Flyway
- MySQL 8.4
- Redis 7.2
- AWS SNS, AWS SES
- Google OAuth, Toss 본인인증
- JUnit 5

## 프로젝트 정보

- Root project: `auth`
- Group: `com.endside`
- 기본 포트: `38080`
- 주요 프로파일: `default`, `dev`, `compose`, `stg-compose`
- 로컬 Docker 구성: MySQL, Redis

## 실행 준비

### Java

- Corretto 21 또는 Java 21 호환 JDK를 설치합니다.
- Windows: https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/what-is-corretto-21.html
- macOS: https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/macos-install.html

### Docker

- Docker Desktop을 설치합니다.
- Windows: https://docs.docker.com/desktop/install/windows-install/
- macOS: https://docs.docker.com/desktop/install/mac-install/

### IntelliJ

- Annotation Processing을 활성화합니다.
- `File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors`
- `Enable annotation processing` 체크

## 로컬 실행

```bash
./gradlew bootRun
```

Windows:

```bat
gradlew.bat bootRun
```

Docker Compose를 직접 실행할 경우:

```bash
docker compose up -d
docker compose down
```

Spring Boot Docker Compose 연동이 활성화되어 있어 IDE 또는 `bootRun` 실행 시 필요한 로컬 컨테이너가 자동으로 기동될 수 있습니다.

## 빌드 및 검증

```bash
./gradlew clean build
./gradlew build -x test --warning-mode all
./gradlew testClasses
```

Windows:

```bat
gradlew.bat clean build
gradlew.bat build -x test --warning-mode all
gradlew.bat testClasses
```

## 설정

기본 설정 파일은 `src/main/resources` 아래에 있습니다.

- `application-default.yml`: 로컬 기본 실행 설정
- `application-dev.yml`: 개발 환경 설정
- `application-compose.yml`: Docker Compose 기반 로컬 실행 설정
- `application-stg-compose.yml`: staging compose 실행 설정

DB, Redis, OAuth, Toss, AWS 값은 환경 변수 placeholder를 기준으로 관리합니다. 새 프로젝트를 시작할 때 실제 운영 비밀값을 커밋하지 않습니다.
