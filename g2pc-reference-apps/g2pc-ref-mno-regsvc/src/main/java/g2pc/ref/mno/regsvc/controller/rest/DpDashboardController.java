package g2pc.ref.mno.regsvc.controller.rest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DpDashboardController {

    private static final String DASHBOARD_URL = "http://localhost:3005/d-solo/c766225a-d5cf-4c9f-99a7-6f8291f407eb/dp-dashboard?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1";

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboardUrl", DASHBOARD_URL);
        return "dashboard";
    }
}
