package com.endside.config.redis;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "redis.cluster")
public class RedisClusterConfigurationProperties {

    List<String> nodes;

    public List<HostInfo> toHostInfo() {
        List<HostInfo> hostInfos = new ArrayList<>();
        for(String node : nodes) {
            String [] components = node.split(":");
            if (components.length == 2) {
                hostInfos.add(new HostInfo(components[0],
                        Integer.parseInt(components[1])));
            }
        }
        return hostInfos;
    }
    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
