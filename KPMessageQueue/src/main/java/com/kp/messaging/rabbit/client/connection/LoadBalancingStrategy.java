package com.kp.messaging.rabbit.client.connection;


import com.google.common.net.HostAndPort;
import com.kp.common.log.Loggable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public enum LoadBalancingStrategy implements Loggable {
    RANDOM,
    ROUND_ROBIN;

    private static final Random rand = new Random(System.nanoTime());

    public static LoadBalancingStrategy fromName(String name) {
        if (name != null) {
            for (LoadBalancingStrategy value : values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
        }
        return null;
    }

    public HostAndPort getNextEndpoint(List<HostAndPort> samples, HostAndPort lastSelectedEndpoint) {
        if (samples != null && samples.size() > 0) {
            switch (this) {
                case RANDOM:
                    List<HostAndPort> filteredSamples = new ArrayList<>();
                    if (lastSelectedEndpoint != null) {
                        for (HostAndPort endpoint : samples) {
                            if (!endpoint.equals(lastSelectedEndpoint)) {
                                filteredSamples.add(endpoint);
                            }
                        }
                    } else {
                        filteredSamples.addAll(samples);
                    }
                    if (filteredSamples.size() > 0) {
                        return filteredSamples.get(rand.nextInt(filteredSamples.size()));
                    }
                    break;
                case ROUND_ROBIN:
                    int id = lastSelectedEndpoint == null ? -1 : samples.indexOf(lastSelectedEndpoint);
                    int nextId = id + 1;
                    if (nextId == samples.size()) {
                        nextId = 0;
                    }
                    getLogger().info("get next endpoint index {} - endpoint: {}", nextId, samples.get(nextId));
                    return samples.get(nextId);
            }
        }
        return null;
    }
}