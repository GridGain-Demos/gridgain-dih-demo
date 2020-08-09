package org.vk.gridgain.dih.web;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AccountsWebApp {
    public static void main(String[] args) {
        SpringApplication.run(AccountsWebApp.class, args);
    }

    @Bean
    public IgniteClient igniteClient() {
        return Ignition.startClient(new ClientConfiguration().setAddresses("localhost"));
    }
}
