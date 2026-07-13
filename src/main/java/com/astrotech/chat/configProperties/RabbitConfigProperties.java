package com.astrotech.chat.configProperties;

import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "spring.rabbitmq")
public record RabbitConfigProperties(
         String url,
        String host,

        String username,

        String password,

        int port,
         String VirtualHost
) {

}
