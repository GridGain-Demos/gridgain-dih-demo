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

package org.vk.gridgain.dih.kafka;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.vk.gridgain.dih.model.Transaction;
import org.vk.gridgain.dih.model.TransactionKey;

class TransactionGenerator {
    private static final Random RANDOM = new Random();

    private static final List<Tuple<String, String>> DATA = new ArrayList<>();

    private long id;

    static {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/accounts.csv"));

            // Skip header.
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                String ccNumber = parts[0];
                String issueCountry = parts[3];

                DATA.add(new Tuple<>(ccNumber, issueCountry));
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Tuple<TransactionKey, Transaction> generate() {
        Tuple<String, String> item = DATA.get(RANDOM.nextInt(DATA.size()));

        String ccNumber = RANDOM.nextDouble() < 0.01 ? "00000" : item._1;
        String country = RANDOM.nextDouble() < 0.01 ? "AA" : item._2;

        TransactionKey key = new TransactionKey(id++, ccNumber);
        Transaction value = new Transaction(country, RANDOM.nextDouble());

        return new Tuple<>(key, value);
    }

    static class Tuple<T1, T2> {
        final T1 _1;
        final T2 _2;

        Tuple(T1 _1, T2 _2) {
            this._1 = _1;
            this._2 = _2;
        }
    }
}
