package g2pc.ref.dc.client.controller.rest;

import g2pc.core.lib.dto.common.security.G2pTokenResponse;
import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.dc.core.lib.service.RequestBuilderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.text.ParseException;

@Controller
@Slf4j
public class DcDashboardController {

    @Value("${dashboard.left_panel_url}")
    private String leftPanelUrl;

    @Value("${dashboard.right_panel_url}")
    private String rightPanelUrl;

    @Value("${dashboard.bottom_panel_url}")
    private String bottomPanelUrl;

    @Value("${dashboard.post_endpoint_url}")
    private String postEndpointUrl;

    @Value("${dashboard.clear_dc_db_endpoint_url}")
    private String clearDcDbEndpointUrl;

    @Value("${keycloak.dc.client.url}")
    private String dcKeyCloakUrl;

    @Value("${keycloak.dc.client.clientId}")
    private String dcClientId;

    @Value("${keycloak.dc.client.clientSecret}")
    private String dcClientSecret;

    @Autowired
    private RequestBuilderService requestBuilderService;

    @GetMapping("/dashboard")
    public String showDashboardPage(Model model) throws IOException, ParseException {
        String jwtToken = requestBuilderService.getValidatedToken(dcKeyCloakUrl, dcClientId, dcClientSecret);

        model.addAttribute("left_panel_url", leftPanelUrl);
        model.addAttribute("right_panel_url", rightPanelUrl);
        model.addAttribute("bottom_panel_url", bottomPanelUrl);
        model.addAttribute("post_endpoint_url", postEndpointUrl);
        model.addAttribute("clear_dc_db_endpoint_url", clearDcDbEndpointUrl);
        model.addAttribute("jwtToken", jwtToken);
        return "dashboard";
    }
}
