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

package org.vk.gridgain.dih.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class Transaction {
    public static final String VALID = "VALID";
    public static final String NO_ACCOUNT = "NO_ACCOUNT";
    public static final String WRONG_COUNTRY = "WRONG_COUNTRY";

    @QuerySqlField(index = true)
    private String country;

    @QuerySqlField
    private double amount;

    @QuerySqlField(index = true)
    private String status;

    public Transaction(String country, double amount) {
        this.country = country;
        this.amount = amount;

        status = VALID;
    }

    public Transaction(Transaction other) {
        country = other.country;
        amount = other.amount;
        status = other.status;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override public String toString() {
        return "Transaction [" +
            "country='" + country + '\'' +
            ", amount=" + amount +
            ", status='" + status + '\'' +
            ']';
    }
}
