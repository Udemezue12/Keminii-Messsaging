package com.astrotech.chat.configProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    private String paystackSecretKey;
    private String flutterwaveSecretKey;
    private String flutterwaveSecretHash;

}
