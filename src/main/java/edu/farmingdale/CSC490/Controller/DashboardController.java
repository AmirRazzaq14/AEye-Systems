package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Dashboard.DashboardService;
import edu.farmingdale.CSC490.Dashboard.Entity.DashboardView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<DashboardView> getDashboardData(@PathVariable int userId) {
        DashboardView view = dashboardService.buildDashboard(userId);
        return ResponseEntity.ok(view);
    }
}
