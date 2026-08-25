package io.confluent.developer;

import java.util.Map;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.Options;

/**
 * kafka-streams 2.3.1's RocksDBTimestampedStore recovers from a missing "keyValueWithTimestamp"
 * column family by matching the RocksDBException message text exactly ("Column family not
 * found: : keyValueWithTimestamp"). Newer rocksdbjni releases (needed here for Apple Silicon
 * native binaries) word that message differently, so the match fails and the store throws
 * instead of recovering. Telling RocksDB to create missing column families itself avoids the
 * exception - and that string match - altogether.
 */
public class CompatibleRocksDBConfigSetter implements RocksDBConfigSetter {

  @Override
  public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
    options.setCreateMissingColumnFamilies(true);
  }

  @Override
  public void close(final String storeName, final Options options) {
  }
}
