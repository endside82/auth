package com.endside.config.redis;

import lombok.Data;

@Data
public class HostInfo {
    private String addr;
    private int port;
    public HostInfo(String addr, int port) {
        this.addr = addr;
        this.port = port;
    }
}
