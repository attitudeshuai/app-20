package com.instrumentroom.controller;

import com.instrumentroom.dto.common.ApiResponse;
import com.instrumentroom.dto.stats.OverviewStats;
import com.instrumentroom.dto.stats.TrendStats;
import com.instrumentroom.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 统计与看板控制器
 */
@RestController
@RequestMapping("/api/stats")
@Tag(name = "统计看板", description = "数据统计与趋势分析")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 总览统计
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "总览统计", description = "获取系统核心指标总览数据（仅管理员）")
    public ResponseEntity<ApiResponse<OverviewStats>> getOverview() {
        OverviewStats response = statsService.getOverviewStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 趋势统计
     */
    @GetMapping("/trend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "趋势统计", description = "获取指定时间范围内的预约趋势数据（仅管理员）")
    public ResponseEntity<ApiResponse<TrendStats>> getTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        TrendStats response = statsService.getTrendStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
