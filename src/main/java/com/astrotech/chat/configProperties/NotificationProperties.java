package com.astrotech.chat.configProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private String termiiBaseUrl;
    private String termiiApiKey;
    private String termiiSenderId;

    private String sendChampBaseUrl;
    private String sendChampApiKey;
    private String sendChampSenderId;


    private String projectName;

    private String brevoApiKey;
    private String brevoUrl;

    private String emailHost;
    private Integer emailPort;
    private String emailUsername;
    private String emailPassword;
    private Boolean emailUseTls;

    private String frontendUrl;
}
