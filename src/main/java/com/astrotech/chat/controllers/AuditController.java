package com.astrotech.chat.controllers;


import com.astrotech.chat.core.GetCalculatedPagination;
import com.astrotech.chat.core.GetCurrentUser;
import com.astrotech.chat.dto.response.AuditLogResponse;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.service.AuditService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@RestController
@Hidden
@RequestMapping("api/v1/audit")
@Tag(name = "Audit Logs")
public class AuditController {
    private final AuditService auditService;
    private final GetCurrentUser getCurrentUser;

    @GetMapping("/me")
    @Ratelimit
    public SliceResponse<AuditLogResponse> getAuditLog(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        return auditService.getMyAuditLog(getCurrentUser.getCurrentUserId(), page, size);
    }

    @GetMapping("/me/recent")
    @Ratelimit
    public List<AuditLogResponse> getRecentLog() {
        return auditService.getRecentActivity(getCurrentUser.getCurrentUserId());
    }

    @GetMapping("/me/summary")
    @Ratelimit
    public Map<String, Object> getSummary() {
        return auditService.getSummary(getCurrentUser.getCurrentUserId());
    }

}
