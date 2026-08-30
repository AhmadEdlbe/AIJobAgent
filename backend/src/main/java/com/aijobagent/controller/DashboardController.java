package com.aijobagent.controller;

import com.aijobagent.dto.DashboardStatsDto;
import com.aijobagent.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final ApplicationService appService;

    public DashboardController(ApplicationService appService){ this.appService=appService; }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> stats(){
        return ResponseEntity.ok(appService.stats());
    }
}
