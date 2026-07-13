package com.astrotech.chat.converters;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public final class OffsetDateTimeConverters {

    private OffsetDateTimeConverters() {
        
    }

    @WritingConverter
    public static class OffsetDateTimeWriteConverter implements Converter<OffsetDateTime, Date> {
        @Override
        public Date convert(OffsetDateTime source) {

            return Date.from(source.toInstant());
        }
    }

    @ReadingConverter
    public static class OffsetDateTimeReadConverter implements Converter<Date, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(Date source) {

            return source.toInstant().atOffset(ZoneOffset.UTC);
        }
    }
}