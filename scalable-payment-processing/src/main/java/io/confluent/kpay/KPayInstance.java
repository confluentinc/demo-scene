/**
 * Copyright 2018 Confluent Inc.
 * <p>
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/AGPL-3.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kpay;

import java.util.Properties;

public class KPayInstance {

    private final KPay kpay;

    public KPayInstance(KPay kpay) {
        this.kpay = kpay;
    }

    public KPay getInstance() {
        return kpay;
    }


    /**
     * Note: dont care about double locking because it is always created on startup in the Servlet Lifecycle.start()
     */
    private static volatile KPayInstance singleton = null;

    /**
     * Only called during initial startup
     **/
    public static KPayInstance getInstance(Properties propertes) {
        if (singleton == null) {

            if (propertes == null) {
                throw new RuntimeException("KPay has not been initialized! -= pass in valid properties and init before use");
            }
            KPayAllInOneImpl kPay = new KPayAllInOneImpl(propertes.getProperty("bootstrap.servers", "localhost:9092"));

            kPay.initializeEnvironment();

            singleton = new KPayInstance(kPay);
            return singleton;
        }
        return singleton;
    }

}
