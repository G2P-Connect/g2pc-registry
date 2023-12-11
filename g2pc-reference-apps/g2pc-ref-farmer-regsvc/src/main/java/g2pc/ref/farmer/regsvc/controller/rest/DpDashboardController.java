package g2pc.ref.farmer.regsvc.controller.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DpDashboardController {

    @GetMapping("/dashboard")
    public String showDashboardPage() {
        return "dashboard";
    }
}
