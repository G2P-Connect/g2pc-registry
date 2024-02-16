package g2pc.ref.farmer.regsvc.controller.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DpDashboardController {

    @Value("${dashboard.dp_dashboard_url}")
    private String dpDashboardUrl;

    @GetMapping("/dashboard")
    public String showDashboardPage(Model model) {
        model.addAttribute("dp_dashboard_url", dpDashboardUrl);
        return "dashboard";
    }


}
