package com.endside.config.ssh;

public class SshConfig {
    public static final String KEY_PATH = "keys/dev-an2-key-bg.pem"; // LOCAL pem key path
    public static final String SSH_SERVER = "ec2-3-34-141-66.ap-northeast-2.compute.amazonaws.com"; // ssh server address. access to db via this server
    public static final int SSH_PORT = 22; // ssh server port
    public static final String SSH_USER = "ec2-user";
    // RDS
    public static final int RDS_L_PORT = 3306;
    public static final String RDS_L_ADDR = "127.0.0.1"; //원격 접속 후 가상으로 포워딩할 포트
    public static final int RDS_R_PORT = 3306; //실제 사용할 데이터베이스 포트
    public static final String RDS_R_ADDR = "dev-an2-db-bg-2nd.cluster-cj5z776p3jn8.ap-northeast-2.rds.amazonaws.com";
    // redis
    public static final int REDIS_L_PORT = 6379;
    public static final String REDIS_L_ADDR = "127.0.0.1";
    public static final int REDIS_R_PORT = 6379;
    public static final String REDIS_R_ADDR = "dev-an2-redis-bg.pc9ga1.0001.apn2.cache.amazonaws.com";
}
