package io.confluent.kpay.rest_iq;

import java.util.Objects;
import java.util.Set;

/**
 * A simple bean that can be JSON serialized via Jersey. Represents a KafkaStreams instance
 * that has a set of state stores.
 * <p>
 * We use this JavaBean based approach as it fits nicely with JSON serialization provided by
 * jax-rs/jersey
 */
public class HostStoreInfo {

    private String host;
    private int port;
    private Set<String> storeNames;

    public HostStoreInfo() {
    }

    public HostStoreInfo(final String host, final int port, final Set<String> storeNames) {
        this.host = host;
        this.port = port;
        this.storeNames = storeNames;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public Set<String> getStoreNames() {
        return storeNames;
    }

    public void setStoreNames(final Set<String> storeNames) {
        this.storeNames = storeNames;
    }

    @Override
    public String toString() {
        return "HostStoreInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", storeNames=" + storeNames +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HostStoreInfo that = (HostStoreInfo) o;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                Objects.equals(storeNames, that.storeNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, storeNames);
    }

    public String getAddress() {
        return String.format("http://%s:%d", getHost(), getPort());
    }
}
