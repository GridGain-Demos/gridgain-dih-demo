/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vk.gridgain.dih.spark;

import org.gridgain.sparkloader.GridGainSparkLoader;
import org.gridgain.sparkloader.GridGainSparkLoader.GridGainSparkLoaderBuilder;

public class AccountsLoader {
    public static void main(String[] args) {
        GridGainSparkLoader loader = null;

        try {
            loader = new GridGainSparkLoaderBuilder()
                .setApplicationName("AccountsLoader")
                .setMaster("local")
                .build("ignite-client-config.xml");

            loader.loadFromCsvToExistingCache("hdfs://localhost:9000/accounts.csv", "Account", true, ",").save();
        }
        finally {
            if (loader != null)
                loader.closeSession();
        }
    }
}
