package com.astrotech.chat.configProperties;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@ConfigurationProperties(prefix = "pingers")
public record PingerProperties(
    List<String> pingUrls){
}
