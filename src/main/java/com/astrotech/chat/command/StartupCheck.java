package com.astrotech.chat.command;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StartupCheck implements CommandLineRunner {

    private final Environment environment;

    @Override
    public void run(String... args) {
        System.out.println(
                "CLOUDINARY_CLOUD_NAME = "
                        + environment.getProperty("CLOUDINARY_CLOUD_NAME")
        );
    }
}
