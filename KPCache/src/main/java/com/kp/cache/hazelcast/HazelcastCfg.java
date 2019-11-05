package com.kp.cache.hazelcast;

import com.kp.cache.InstanceConfig;

public class HazelcastCfg extends InstanceConfig {
    public static int SERIALIZABLE_FACTORY_ID = 1;

    public HazelcastCfg(String configFile) {
        super(configFile);
    }


}
