/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/AGPL-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kpay.utils;

import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Runs an in-memory, "embedded" instance of a ZooKeeper server.
 *
 * The ZooKeeper server instance is automatically started when you create a new instance of this class.
 */
class ZooKeeperEmbedded {

  private static final Logger log = LoggerFactory.getLogger(ZooKeeperEmbedded.class);

  private final TestingServer server;

  /**
   * Creates and starts a ZooKeeper instance.
   *
   */
  public ZooKeeperEmbedded() throws Exception {
    log.debug("Starting embedded ZooKeeper server...");
    this.server = new TestingServer();
    log.debug("Embedded ZooKeeper server at {} uses the temp directory at {}",
            server.getConnectString(), server.getTempDirectory());
  }

  public void stop() throws IOException {
    log.debug("Shutting down embedded ZooKeeper server at {} ...", server.getConnectString());
    server.close();
    log.debug("Shutdown of embedded ZooKeeper server at {} completed", server.getConnectString());
  }

  /**
   * The ZooKeeper connection string aka `zookeeper.connect` in `hostnameOrIp:port` format.
   * Example: `127.0.0.1:2181`.
   *
   * You can use this to e.g. tell Kafka brokers how to connect to this instance.
   */
  public String connectString() {
    return server.getConnectString();
  }

}