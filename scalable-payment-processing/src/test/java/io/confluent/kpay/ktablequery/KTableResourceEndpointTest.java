package io.confluent.kpay.ktablequery;

import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class KTableResourceEndpointTest {

    private KTableResourceEndpoint<String, String> ktableRestServices;

    @Test
    public void size() {
        KTableRestClient<String, String> restClient = new KTableRestClient<String, String>(true, null, "store") {
            @Override
            protected List<HostStoreInfo> getHostStoreInfos() {
                return Collections.singletonList(new HostStoreInfo("localhost", 12345, new HashSet<>(Collections.singletonList("store"))));
            }
        };

        int size = restClient.size();
        Assert.assertEquals(999, size);
    }

    @Test
    public void get() {
        KTableRestClient<String, String> restClient = new KTableRestClient<String, String>(true,null, "store") {
            @Override
            protected List<HostStoreInfo> getHostStoreInfos() {
                return Collections.singletonList(new HostStoreInfo("localhost", 12345, new HashSet<>(Collections.singletonList("store"))));
            }

            @Override
            protected HostStoreInfo getHostStoreInfoForKey(String k) {
                return new HostStoreInfo("localhost", 12345, new HashSet<>(Collections.singletonList("store")));
            }
        };

        String result = restClient.get("testKey");
        Assert.assertEquals("testKey-testValue", result);
    }

    @After
    public void teardown() {
        ktableRestServices.stop();

    }
    @Before
    public void setup() {
        ktableRestServices = new KTableResourceEndpoint<String, String>(() -> new TestKeyValueStore()){};

        Properties streamsConfig = new Properties();
        streamsConfig.put(StreamsConfig.APPLICATION_SERVER_CONFIG, "localhost:12345");
        ktableRestServices.start(streamsConfig);
    }

    static class TestKeyValueStore implements ReadOnlyKeyValueStore<String, String>{

        @Override
        public String get(String key) {
            return key + "testValue";
        }

        @Override
        public KeyValueIterator<String, String> range(String from, String to) {
            return null;
        }

        @Override
        public KeyValueIterator<String, String> all() {
            return null;
        }

        @Override
        public long approximateNumEntries() {
            return 999;
        }
    }
}