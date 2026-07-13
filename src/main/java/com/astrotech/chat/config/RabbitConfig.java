package com.astrotech.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionFactoryBeanConfigurer;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RabbitConfig {
    private final RabbitProperties rabbitProperties;
    private final RabbitConnectionFactoryBeanConfigurer rabbitConfigurer;

    @Bean
    RabbitTemplate rabbitTemplate(
            CachingConnectionFactory connectionFactory,
            RabbitTemplateConfigurer configurer) {

        var template = new RabbitTemplate();

        configurer.configure(template, connectionFactory);

        return template;
    }

    @Bean
    public RabbitTemplateConfigurer customRabbitTemplate() {
        return new RabbitTemplateConfigurer(rabbitProperties);

    }

    @Bean
    public CachingConnectionFactory customRabbitConnectionFactory() {
        try {

            var factoryBean = new RabbitConnectionFactoryBean();

            rabbitConfigurer.configure(factoryBean);

            factoryBean.afterPropertiesSet();

            var rabbitConnectionFactory = factoryBean.getObject();

            var caching = new CachingConnectionFactory(rabbitConnectionFactory);

            caching.setPublisherConfirmType(
                    CachingConnectionFactory.ConfirmType.CORRELATED);

            caching.setPublisherReturns(true);

            caching.setChannelCacheSize(100);

            caching.setConnectionNameStrategy(cf -> "chat-service");

            return caching;

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Failed to connect");
        }
    }
}
