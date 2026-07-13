package com.astrotech.chat.core;

public class TrimWhiteSpace {
    public static String trimWhiteSpace(String str) {
        return str.trim();
    }
    public static String trimWhiteSpaceWithUpperCase(String str, boolean withUpperCase){
        if (withUpperCase)  {
            return str.trim().toUpperCase();

        }
        else {
            return str.trim().toLowerCase();

        }
    }

    public static String sanitize(String url) {

        return url
                .replace("https://", "")
                .replace("http://", "")
                .replaceAll("[^a-zA-Z0-9]", "-");

    }

}
