package com.kp.messaging.rabbit.client.connection;


import com.kp.common.data.vo.HostAndPort;
import com.kp.common.data.vo.UserNameAndPassword;
import com.kp.common.log.Loggable;
import com.rabbitmq.client.Address;
import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.Connections;
import net.jodah.lyra.config.Config;
import net.jodah.lyra.config.ConfigurableConnection;
import net.jodah.lyra.config.RecoveryPolicies;
import net.jodah.lyra.config.RetryPolicies;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnectionGen implements Loggable {

    private UserNameAndPassword credential;
    private List<HostAndPort> endpoints;

    private Config config;
    private ConnectionOptions options;

    private boolean retry = false;

    public RabbitMQConnectionGen() {
        this.config = new Config();
        this.config.withRecoveryPolicy(RecoveryPolicies.recoverAlways());
        if (retry) {
            this.config.withRetryPolicy(RetryPolicies.retryAlways());
        } else {
            this.config.withRetryPolicy(RetryPolicies.retryNever());

        }

        this.options = new ConnectionOptions();
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public void init() {
        if (this.getCredential() != null) {
            getLogger().debug("connect with username and password: {}, {}", this.getCredential().getUsername(),
                    this.getCredential().getPassword());
            this.options.withUsername(this.getCredential().getUsername());
            this.options.withPassword(this.getCredential().getPassword());
        }
        if (this.getEndpoints() != null) {
            ArrayList<Address> addresses = new ArrayList();
            for (int i = 0; i < endpoints.size(); i++) {
                HostAndPort endpoint = this.getEndpoints().get(i);
                getLogger().debug("add address: {}", endpoint);
                if (endpoint != null && endpoint.getHost() != null && !endpoint.getHost().trim().isEmpty()
                        ) {
                    if (endpoint.getPort() > 0) {
                        addresses.add(new Address(endpoint.getHost(), endpoint.getPort()));
                    } else

                    {

                        String vhost = "amqp://%s:%s@%s";
                        vhost = String.format(vhost, this.getCredential().getUsername(), this.getCredential().getPassword(), endpoint.getHost());
                        try {
                            options.withUri(URI.create(vhost));
                        } catch (Exception e) {
                            getLogger().error("init error create uri {}", e);
                        }
                    }
                }

            }
            if (addresses.size() > 0) {
                this.options.withAddresses(addresses.toArray(new Address[addresses.size()]));
            }
        }


    }

    public RabbitMQConnection getConnection() throws Exception {

        long sleepTime = -1;
        ConfigurableConnection connection = null;

        try {
            connection = Connections.create(this.options, this.config);
        } catch (IOException ioException) {
            getLogger().debug("connection error", ioException);
            throw ioException;
        } catch (TimeoutException timeoutException) {
            throw timeoutException;
        }


        return new RabbitMQConnection(connection);
    }

    public UserNameAndPassword getCredential() {
        return credential;
    }

    public void setCredential(UserNameAndPassword credential) {
        this.credential = credential;
    }

    public List<HostAndPort> getEndpoints() {
        return endpoints;
    }

    public void addEndpoints(Collection<HostAndPort> endpoints) {
        if (endpoints == null) {
            return;
        }
        if (this.endpoints == null) {
            this.endpoints = new ArrayList<>();
        }
        this.endpoints.addAll(endpoints);
    }

    public void addEndpoints(HostAndPort... endpoints) {
        if (endpoints == null) {
            return;
        }
        if (this.endpoints == null) {
            this.endpoints = new ArrayList<>();
        }
        this.endpoints.addAll(Arrays.asList(endpoints));
    }


}
