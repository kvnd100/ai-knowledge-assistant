package com.aiknowledge.assistant.controller;

import com.aiknowledge.assistant.dto.dashboard.DashboardStatsResponse;
import com.aiknowledge.assistant.entity.User;
import com.aiknowledge.assistant.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Account activity summary")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get usage statistics for the authenticated user")
    public DashboardStatsResponse stats(@AuthenticationPrincipal User user) {
        return dashboardService.stats(user);
    }
}
