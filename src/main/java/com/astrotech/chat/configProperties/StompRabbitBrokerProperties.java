package com.astrotech.chat.configProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@Setter
@Getter
@ConfigurationProperties(prefix = "app.stomp")
public class StompRabbitBrokerProperties {


        private int relayPort;


}
