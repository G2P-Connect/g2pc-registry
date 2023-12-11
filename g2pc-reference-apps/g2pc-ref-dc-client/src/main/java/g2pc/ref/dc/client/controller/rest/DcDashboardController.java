package g2pc.ref.dc.client.controller.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DcDashboardController {

    @Value("${dashboard.left_panel_url}")
    private String leftPanelUrl;

    @Value("${dashboard.right_panel_url}")
    private String rightPanelUrl;

    @Value("${dashboard.bottom_panel_url}")
    private String bottomPanelUrl;

    @GetMapping("/dashboard")
    public String showDashboardPage(Model model) {
        model.addAttribute("left_panel_url", leftPanelUrl);
        model.addAttribute("right_panel_url", rightPanelUrl);
        model.addAttribute("bottom_panel_url", bottomPanelUrl);
        return "dashboard";
    }
}
