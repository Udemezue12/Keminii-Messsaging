package com.astrotech.chat.configProperties;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "cors")
public record AllowedOrigins(
        List<String> allowedOrigins,
        Set<String>  allowedEmailDomains
){


}
