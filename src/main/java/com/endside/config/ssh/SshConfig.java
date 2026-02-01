package com.endside.config.ssh;

/**
 * SSH 터널링 설정
 * 주의: 실제 배포 시에는 환경 변수나 설정 파일을 통해 값을 주입받아야 합니다.
 * 아래 값들은 예시/템플릿이며, 실제 운영 환경의 값을 하드코딩하지 마세요.
 */
public class SshConfig {
    // SSH 접속 설정 - 환경에 맞게 수정 필요
    public static final String KEY_PATH = System.getenv().getOrDefault("SSH_KEY_PATH", "keys/your-key.pem");
    public static final String SSH_SERVER = System.getenv().getOrDefault("SSH_SERVER", "your-ssh-server.compute.amazonaws.com");
    public static final int SSH_PORT = 22;
    public static final String SSH_USER = System.getenv().getOrDefault("SSH_USER", "ec2-user");

    // RDS 포트 포워딩 설정
    public static final int RDS_L_PORT = 3306;
    public static final String RDS_L_ADDR = "127.0.0.1";
    public static final int RDS_R_PORT = 3306;
    public static final String RDS_R_ADDR = System.getenv().getOrDefault("RDS_ADDRESS", "your-rds-cluster.rds.amazonaws.com");

    // Redis 포트 포워딩 설정
    public static final int REDIS_L_PORT = 6379;
    public static final String REDIS_L_ADDR = "127.0.0.1";
    public static final int REDIS_R_PORT = 6379;
    public static final String REDIS_R_ADDR = System.getenv().getOrDefault("REDIS_ADDRESS", "your-redis.cache.amazonaws.com");
}
