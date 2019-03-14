package io.confluent.kpay.rest_iq;

import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class WindowKTableResourceEndpointTest {

    private WindowKTableResourceEndpoint<String, String> ktableRestServices;

    @Test
    public void get() {
        KTableRestClient<String, String> restClient = new KTableRestClient<String, String>(false,null, "store") {
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
        ktableRestServices = new WindowKTableResourceEndpoint<String, String>(() -> new TestKeyValueStore()){};

        Properties streamsConfig = new Properties();
        streamsConfig.put(StreamsConfig.APPLICATION_SERVER_CONFIG, "localhost:12345");
        ktableRestServices.start(streamsConfig);
    }

    static class TestKeyValueStore implements ReadOnlyWindowStore<String, String> {
        @Override
        public String fetch(String key, long time) {
            return key + "-testValue";
        }

        @Override
        public WindowStoreIterator<String> fetch(String key, long timeFrom, long timeTo) {
            return null;
        }

        @Override
        public KeyValueIterator<Windowed<String>, String> fetch(String from, String to, long timeFrom, long timeTo) {
            return null;
        }

        @Override
        public KeyValueIterator<Windowed<String>, String> all() {
            return null;
        }

        @Override
        public KeyValueIterator<Windowed<String>, String> fetchAll(long timeFrom, long timeTo) {
            return null;
        }

//        @Override
//        public String get(String key) {
//            return "test-value-" + key;
//        }
//
//        @Override
//        public KeyValueIterator<String, String> range(String from, String to) {
//            return null;
//        }
//
//        @Override
//        public KeyValueIterator<String, String> all() {
//            return null;
//        }
//
//        @Override
//        public long approximateNumEntries() {
//            return 999;
//        }
    }
}