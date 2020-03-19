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

import java.util.Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.vk.gridgain.dih.kafka.TransactionGenerator.Tuple;
import org.vk.gridgain.dih.model.Transaction;
import org.vk.gridgain.dih.model.TransactionKey;

public class TransactionsProducer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final JsonNode KEY_SCHEMA;

    private static final JsonNode VALUE_SCHEMA;

    static {
        ArrayNode keyFields = MAPPER.createArrayNode();

        keyFields.add(MAPPER.createObjectNode().put("type", "int64").put("optional", false).put("field", "id"));
        keyFields.add(MAPPER.createObjectNode().put("type", "string").put("optional", false).put("field", "ccNumber"));

        KEY_SCHEMA = MAPPER.createObjectNode()
            .put("name", "org.vk.gridgain.dih.model.TransactionKey")
            .put("type", "struct")
            .put("optional", false)
            .set("fields", keyFields);

        ArrayNode valueFields = MAPPER.createArrayNode();

        valueFields.add(MAPPER.createObjectNode().put("type", "string").put("optional", false).put("field", "country"));
        valueFields.add(MAPPER.createObjectNode().put("type", "double").put("optional", false).put("field", "amount"));
        valueFields.add(MAPPER.createObjectNode().put("type", "string").put("optional", false).put("field", "status"));

        VALUE_SCHEMA = MAPPER.createObjectNode()
            .put("name", "org.vk.gridgain.dih.model.Transaction")
            .put("type", "struct")
            .put("optional", false)
            .set("fields", valueFields);
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();

        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.connect.json.JsonSerializer");
        props.put("value.serializer", "org.apache.kafka.connect.json.JsonSerializer");

        try (Producer<JsonNode, JsonNode> producer = new KafkaProducer<>(props)) {
            TransactionGenerator generator = new TransactionGenerator();

            for (int i = 0; i < 1_000_000; i++) {
                Tuple<TransactionKey, Transaction> entry = generator.generate();

                ObjectNode key = MAPPER.createObjectNode();

                key.set("schema", KEY_SCHEMA);
                key.set("payload", MAPPER.valueToTree(entry._1));

                ObjectNode value = MAPPER.createObjectNode();

                value.set("schema", VALUE_SCHEMA);
                value.set("payload", MAPPER.valueToTree(entry._2));

                producer.send(new ProducerRecord<>("ignite.TRANSACTIONS", key, value));

                Thread.sleep(10);
            }
        }
    }
}
