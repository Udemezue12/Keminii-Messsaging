package com.astrotech.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.util.MimeTypeUtils;

@Configuration
@RequiredArgsConstructor
public class MessagingConfig {
    private final ObjectMapper objectMapper;

    @Bean
    public MappingJackson2MessageConverter jackson2MessageConverter() {

        var messagingObjectMapper = objectMapper.copy();

        messagingObjectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        messagingObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        var converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(messagingObjectMapper);

        var resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        converter.setContentTypeResolver(resolver);

        return converter;
    }
}
