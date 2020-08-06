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

package org.vk.gridgain.dih.ignite;

import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;

public class FraudChecker {
    public static void main(String[] args) throws InterruptedException {
        Ignite ignite = Ignition.start("ignite-client-config.xml");

        while (true) {
            Thread.sleep(3000);

            printNoAccountTxs(ignite);
            printWrongCountryTxs(ignite);

            System.out.println();
            System.out.println("====================");
        }
    }

    private static void printNoAccountTxs(Ignite ignite) {
        List<List<?>> result = ignite
            .cache("TRANSACTIONS")
            .query(new SqlFieldsQuery(
                "SELECT id, ccNumber, amount " +
                "FROM Transaction " +
                "WHERE status = 'NO_ACCOUNT' " +
                "LIMIT 10"))
            .getAll();

        System.out.println();
        System.out.println("Transactions associated with a non-existent account:");

        for (List<?> row : result) {
            System.out.println("    {id: " + row.get(0) + ", ccNumber: " + row.get(1) + ", amount: " + row.get(2) + "}");
        }
    }

    private static void printWrongCountryTxs(Ignite ignite) {
        List<List<?>> result = ignite
            .cache("TRANSACTIONS")
            .query(new SqlFieldsQuery(
                "SELECT a.ccNumber, CONCAT(a.firstName, ' ', a.lastName) as name, a.issueCountry, t.country " +
                "FROM Account a, Transaction t " +
                "WHERE a.ccNumber = t.ccNumber " +
                "AND t.status = 'WRONG_COUNTRY' " +
                "LIMIT 10"))
            .getAll();

        System.out.println();
        System.out.println("Transactions occurred in an unexpected country:");

        for (List<?> row : result) {
            System.out.println("    {ccNumber: " + row.get(0) + ", name: " + row.get(1) + ", issueCountry: " + row.get(2) + ", txCountry: " + row.get(3) + "}");
        }
    }
}
