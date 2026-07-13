package com.astrotech.chat.responseBuilder;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@RequiredArgsConstructor
public class ApiResponse<T>{
    private final boolean success;
    private final String message;
    private final T data;
    private final String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss a"));
}
