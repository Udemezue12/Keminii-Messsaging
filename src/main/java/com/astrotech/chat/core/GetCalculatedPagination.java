package com.astrotech.chat.core;


public record GetCalculatedPagination(int page, int size) {
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "15";
    private static final int MAX_PAGE_SIZE = 100;
    
    public GetCalculatedPagination {
        page = Math.max(0, page);
        size = Math.clamp(size, 1, MAX_PAGE_SIZE);
    }
}
