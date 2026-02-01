-- Docker 초기화 SQL
-- 주의: 이 파일은 개발/테스트 환경용입니다.
-- 운영 환경에서는 별도의 보안된 방식으로 사용자를 생성하세요.

CREATE DATABASE IF NOT EXISTS mydatabase;

-- 개발 환경용 사용자 생성 (운영 환경에서는 강력한 비밀번호 사용 필요)
-- 비밀번호는 docker-compose.yaml의 MYSQL_PASSWORD 환경변수와 일치해야 합니다.
CREATE USER IF NOT EXISTS 'myuser'@'%' IDENTIFIED BY 'changeme';
GRANT SELECT, INSERT, UPDATE, DELETE ON `mydatabase`.* TO 'myuser'@'%';

FLUSH PRIVILEGES;