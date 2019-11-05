package com.kp.cache.hazelcast;

import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import static com.kp.cache.hazelcast.HazelcastCfg.SERIALIZABLE_FACTORY_ID;

public abstract class IdDataSerializable implements IdentifiedDataSerializable {
    int id;

    public IdDataSerializable(int id) {
        this.id = id;
    }

    @Override
    public int getFactoryId() {
        return SERIALIZABLE_FACTORY_ID;
    }

    @Override
    public int getId() {
        return id;
    }
}
