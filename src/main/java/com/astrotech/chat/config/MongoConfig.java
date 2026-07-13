package com.astrotech.chat.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.astrotech.chat.converters.OffsetDateTimeConverters.OffsetDateTimeReadConverter;
import com.astrotech.chat.converters.OffsetDateTimeConverters.OffsetDateTimeWriteConverter;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.astrotech.chat.repositories")
public class MongoConfig {
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);

    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {

        return new MongoCustomConversions(List.of(
                new OffsetDateTimeReadConverter(),
                new OffsetDateTimeWriteConverter()));
    }

    @Bean
    MongoClientSettingsBuilderCustomizer mongoPoolCustomizer() {
        return builder -> builder.applyToConnectionPoolSettings(pool -> pool
                .maxSize(200)
                .minSize(20)
                .maxConnectionIdleTime(60, TimeUnit.SECONDS)
                .maxWaitTime(10, TimeUnit.SECONDS)
                .maxConnectionLifeTime(30, TimeUnit.MINUTES));
    }

}
