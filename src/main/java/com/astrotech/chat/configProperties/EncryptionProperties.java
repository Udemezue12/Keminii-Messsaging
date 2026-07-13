package com.astrotech.chat.configProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.encryption")
public class EncryptionProperties {
    private String aesAlgorithm;
    private int gcmTagLength;
    private int ivLength;
    private String rsaAlgorithm;
    private String serverKeyHex;
    private String serverSecret;
    private boolean productionMode;
}
