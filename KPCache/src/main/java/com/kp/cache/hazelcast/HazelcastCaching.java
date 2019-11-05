package com.kp.cache.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.kp.cache.KCache;

public class HazelcastCaching implements KCache {

    private HazelcastInstance hazelcastInstance;
    private boolean isClient;
    private HazelcastCfg hazelcastCfg;

    public HazelcastCaching(boolean isClient, HazelcastCfg hazelcastCfg) {
        this.isClient = isClient;
        this.hazelcastCfg = hazelcastCfg;
    }

    @Override
    public boolean isAlive() {
        return hazelcastInstance != null;
    }

    @Override
    public <K, V> HazelcastMap<K, V> getMap(String name) {
        return new HazelcastMap<>(hazelcastInstance.getMap(name));
    }

    @Override
    public void init() throws Exception {

        if (isClient) {
            ClientConfig config = new XmlClientConfigBuilder(hazelcastCfg.getFileConfig()).build();
            hazelcastInstance = HazelcastClient.newHazelcastClient(config);

        } else {
            Config config = new XmlConfigBuilder(hazelcastCfg.getFileConfig()).build();
            hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        }

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        hazelcastInstance.shutdown();

    }

    @Override
    public void process() throws Exception {

    }
}
