package org.vk.gridgain.dih.web;/*
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.cache.Cache;
import org.apache.commons.lang.StringUtils;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.vk.gridgain.dih.model.Account;
import org.vk.gridgain.dih.model.AccountKey;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.get;
import static spark.Spark.post;

public class AccountsWebApp {
    public static void main(String[] args) throws Exception {
        IgniteClient ignite = Ignition.startClient(new ClientConfiguration().setAddresses("localhost"));

        ClientCache<AccountKey, Account> accounts = ignite.cache("ACCOUNTS");
        ClientCache<String, Set<String>> travels = ignite.cache("TRAVELS");

        get("/accounts", (req, res) -> {
            Collection<Row> rows = new ArrayList<>();

            for (Cache.Entry<AccountKey, Account> entry : accounts.query(new ScanQuery<AccountKey, Account>())) {
                String ccNumber = entry.getKey().getCcNumber();
                String firstName = entry.getValue().getFirstName();
                String lastName = entry.getValue().getLastName();
                String issueCountry = entry.getValue().getIssueCountry();

                Set<String> travelCountriesSet = travels.get(ccNumber);

                String travelCountries = travelCountriesSet != null ? StringUtils.join(travelCountriesSet, ',') : "";

                rows.add(new Row(ccNumber, firstName, lastName, issueCountry, travelCountries));
            }

            return new ModelAndView(Collections.singletonMap("rows", rows), "accounts.vm");
        }, new VelocityTemplateEngine());

        post("/accounts", (req, res) -> {
            String ccNumber = req.queryParams("ccNumber");
            String travelCountries = req.queryParams("travelCountries");

            Set<String> travelCountriesSet = new HashSet<>();

            for (String country : travelCountries.split(",")) {
                travelCountriesSet.add(country.toUpperCase());
            }

            travels.put(ccNumber, travelCountriesSet);

            res.redirect("/accounts");

            return null;
        });
    }

    public static class Row {
        private String ccNumber;

        private String firstName;

        private String lastName;

        private String issueCountry;

        private String travelCountries;

        Row(String ccNumber, String firstName, String lastName, String issueCountry, String travelCountries) {
            this.ccNumber = ccNumber;
            this.firstName = firstName;
            this.lastName = lastName;
            this.issueCountry = issueCountry;
            this.travelCountries = travelCountries;
        }

        public String getCcNumber() {
            return ccNumber;
        }

        public void setCcNumber(String ccNumber) {
            this.ccNumber = ccNumber;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getIssueCountry() {
            return issueCountry;
        }

        public void setIssueCountry(String issueCountry) {
            this.issueCountry = issueCountry;
        }

        public String getTravelCountries() {
            return travelCountries;
        }

        public void setTravelCountries(String travelCountries) {
            this.travelCountries = travelCountries;
        }
    }
}
