package com.kp.cache.ignite;

import com.kp.cache.KCache;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

public class IgniteCaching implements KCache {
    Ignite instance;
    IgniteCfg igniteCfg;

    public IgniteCaching(IgniteCfg igniteCfg) {
        this.igniteCfg = igniteCfg;
    }

    @Override
    public void init() throws Exception {
        instance = Ignition.start(igniteCfg.getFileConfig());
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
        instance.close();
    }

    @Override
    public void process() throws Exception {
    }

    @Override
    public boolean isAlive() {
        if (instance != null) {
            return true;
        }
        return false;
    }

    @Override
    public <K, V> IgniteMap<K, V> getMap(String name) {
        return new IgniteMap<>(instance.cache(name), false);
    }


}
