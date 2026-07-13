package com.astrotech.chat.service;

import com.astrotech.chat.core.GetPageRequest;
import com.astrotech.chat.customCache.CustomCacheable;
import com.astrotech.chat.dto.response.AuditLogResponse;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.entites.AuditLog;
import com.astrotech.chat.mappers.AuditMapper;
import com.astrotech.chat.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @CustomCacheable(value = "myPersonalAuditLog",key = "#userId + '-' + #page + '-' + #size")
    public SliceResponse<AuditLogResponse> getMyAuditLog(String userId,int page, int size){
        size = Math.min(size, 50);

        var pageable = GetPageRequest.getPageableWithSorting(page, size, "createdAt", false);
        var results = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        var content = results.getContent()
                .stream()
                .map(AuditMapper::getAuditLogResponse)
                .toList();
        return new SliceResponse<>(
                content,
                page,
                size,
                results.hasNext(),
                results.hasPrevious()
                );
    }
    @CustomCacheable(value = "recent-activity", key = "#userId")
    public List<AuditLogResponse> getRecentActivity(String userId) {

        var since = Instant.now().minus(24, ChronoUnit.HOURS);
        return auditLogRepository
                .findByUserIdAndCreatedAtAfter(userId, since)
                .stream().map(AuditMapper::getAuditLogResponse).toList();

    }
    @CustomCacheable(value = "get-summary", key = "#userId")
    public Map<String, Object> getSummary(String userId) {
        var since30d = Instant.now().minus(30, ChronoUnit.DAYS);
        var recent = auditLogRepository.findByUserIdAndCreatedAtAfter(userId, since30d);

        var actionCounts = recent.stream()
                .collect(Collectors.groupingBy(
                        AuditLog::getAction, Collectors.counting()));

        return Map.of(
                "totalEvents", recent.size(),
                "periodDays", 30,
                "actionBreakdown", actionCounts,
                "lastLogin", Objects.requireNonNull(recent.stream()
                        .filter(e -> "USER_LOGIN".equals(e.getAction()))
                        .map(AuditLog::getCreatedAt)
                        .max(Instant::compareTo).orElse(null))
        );}




}
