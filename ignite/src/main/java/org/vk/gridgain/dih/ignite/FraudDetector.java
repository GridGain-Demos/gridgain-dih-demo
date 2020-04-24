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

import java.util.Objects;
import java.util.Set;
import javax.cache.Cache;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheInterceptorAdapter;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.vk.gridgain.dih.model.Account;
import org.vk.gridgain.dih.model.AccountKey;
import org.vk.gridgain.dih.model.Transaction;
import org.vk.gridgain.dih.model.TransactionKey;

public class FraudDetector extends CacheInterceptorAdapter<TransactionKey, Transaction> {
    @IgniteInstanceResource
    private transient Ignite ignite;

    @Override public Transaction onBeforePut(Cache.Entry<TransactionKey, Transaction> entry, Transaction newVal) {
        String status = null;

        String ccNumber = entry.getKey().getCcNumber();

        Account account = ignite.<AccountKey, Account>cache("ACCOUNTS").get(new AccountKey(ccNumber));

        if (account == null) {
            status = Transaction.NO_ACCOUNT;
        }
        else {
            String txCountry = newVal.getCountry();

            if (!Objects.equals(txCountry, account.getIssueCountry())) {
                Set<String> travelCountries = ignite.<String, Set<String>>cache("TRAVELS").get(ccNumber);

                if (travelCountries == null || !travelCountries.contains(txCountry))
                    status = Transaction.WRONG_COUNTRY;
            }
        }

        if (status != null) {
            newVal = new Transaction(newVal);

            newVal.setStatus(status);
        }

        return newVal;
    }
}
