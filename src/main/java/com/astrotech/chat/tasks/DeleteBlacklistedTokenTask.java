package com.astrotech.chat.tasks;

import com.astrotech.chat.service.BlacklistedTokenService;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;


import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteBlacklistedTokenTask {


    private final BlacklistedTokenService tokenService;

    @Job(name = "cleanup-expired-blacklisted-tokens", retries = 3)
    public void cleanupExpiredTokens() {
        tokenService.cleanupExpiredTokens();
    }
}
