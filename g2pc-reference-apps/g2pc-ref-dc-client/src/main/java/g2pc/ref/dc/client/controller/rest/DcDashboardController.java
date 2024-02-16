package g2pc.ref.dc.client.controller.rest;

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

    @Value("${sunbird.enabled}")
    private Boolean sunbirdEnabled;

    @Value("${dashboard.left_panel_url}")
    private String leftPanelUrl;

    @Value("${dashboard.right_panel_url}")
    private String rightPanelUrl;

    @Value("${dashboard.bottom_panel_url}")
    private String bottomPanelUrl;

    @Value("${dashboard.post_https_endpoint_url}")
    private String postHttpsEndpointUrl;

    @Value("${dashboard.clear_dc_db_endpoint_url}")
    private String clearDcDbEndpointUrl;

    @Value("${keycloak.dc.client.url}")
    private String dcKeyCloakUrl;

    @Value("${keycloak.dc.client.clientId}")
    private String dcClientId;

    @Value("${keycloak.dc.client.clientSecret}")
    private String dcClientSecret;

    @Value("${dashboard.left_panel_data_endpoint_url}")
    private String leftPanelDataEndpointUrl;

    @Value("${dashboard.sftp_post_endpoint_url}")
    private String sftpPostEndpointUrl;

    @Value("${dashboard.sftp_dc_data_endpoint_url}")
    private String sftpDcDataEndpointUrl;

    @Value("${dashboard.sftp_dp1_data_endpoint_url}")
    private String sftpDp1DataEndpointUrl;

    @Value("${dashboard.sftp_dp2_data_endpoint_url}")
    private String sftpDp2DataEndpointUrl;

    @Value("${dashboard.dc_status_endpoint_url}")
    private String dcStatusEndpointUrl;

    @Value("${dashboard.sftp_left_panel_url}")
    private String sftpLeftPanelUrl;

    @Value("${dashboard.sftp_right_panel_url}")
    private String sftpRightPanelUrl;

    @Value("${dashboard.sftp_bottom_panel_url}")
    private String sftpBottomPanelUrl;

    @Value("${dashboard.https_sunbird_left_panel_url}")
    private String httpsSunbirdLeftPanelUrl;

    @Value("${dashboard.https_sunbird_right_panel_url}")
    private String httpsSunbirdRightPanelUrl;

    @Value("${dashboard.https_sunbird_bottom_panel_url}")
    private String httpsSunbirdBottomPanelUrl;

    @Value("${dashboard.sftp_sunbird_left_panel_url}")
    private String sftpSunbirdLeftPanelUrl;

    @Value("${dashboard.sftp_sunbird_right_panel_url}")
    private String sftpSunbirdRightPanelUrl;

    @Value("${dashboard.sftp_sunbird_bottom_panel_url}")
    private String sftpSunbirdBottomPanelUrl;

    @Autowired
    private RequestBuilderService requestBuilderService;

    @GetMapping("/dashboard/https")
    public String showDashboardPage(Model model) throws IOException, ParseException {
        String jwtToken = requestBuilderService.getValidatedToken(dcKeyCloakUrl, dcClientId, dcClientSecret);
        if (Boolean.TRUE.equals(sunbirdEnabled)) {
            model.addAttribute("left_panel_url", httpsSunbirdLeftPanelUrl);
            model.addAttribute("right_panel_url", httpsSunbirdRightPanelUrl);
            model.addAttribute("bottom_panel_url", httpsSunbirdBottomPanelUrl);
            log.info("httpsSunbirdLeftPanelUrl: {}", httpsSunbirdLeftPanelUrl);
            log.info("httpsSunbirdRightPanelUrl: {}", httpsSunbirdRightPanelUrl);
            log.info("httpsSunbirdBottomPanelUrl: {}", httpsSunbirdBottomPanelUrl);
        } else {
            model.addAttribute("left_panel_url", leftPanelUrl);
            model.addAttribute("right_panel_url", rightPanelUrl);
            model.addAttribute("bottom_panel_url", bottomPanelUrl);
        }
        model.addAttribute("post_https_endpoint_url", postHttpsEndpointUrl);
        model.addAttribute("clear_dc_db_endpoint_url", clearDcDbEndpointUrl);
        model.addAttribute("jwtToken", jwtToken);
        model.addAttribute("left_panel_data_endpoint_url", leftPanelDataEndpointUrl);
        model.addAttribute("dc_status_endpoint_url", dcStatusEndpointUrl);
        return "dashboardHttps";
    }

    @GetMapping("/dashboard/sftp")
    public String showDashboardSftpPage(Model model) throws IOException, ParseException {
        String jwtToken = requestBuilderService.getValidatedToken(dcKeyCloakUrl, dcClientId, dcClientSecret);
        if (Boolean.TRUE.equals(sunbirdEnabled)) {
            model.addAttribute("sftp_left_panel_url", sftpSunbirdLeftPanelUrl);
            model.addAttribute("sftp_right_panel_url", sftpSunbirdRightPanelUrl);
            model.addAttribute("sftp_bottom_panel_url", sftpSunbirdBottomPanelUrl);
        } else {
            model.addAttribute("sftp_left_panel_url", sftpLeftPanelUrl);
            model.addAttribute("sftp_right_panel_url", sftpRightPanelUrl);
            model.addAttribute("sftp_bottom_panel_url", sftpBottomPanelUrl);
        }
        model.addAttribute("sftp_post_endpoint_url", sftpPostEndpointUrl);
        model.addAttribute("clear_dc_db_endpoint_url", clearDcDbEndpointUrl);
        model.addAttribute("jwtToken", jwtToken);
        return "dashboardSftp";
    }

    @GetMapping("/dashboard/sftp/sse")
    public String showDashboardSftpPageSse(Model model) throws IOException, ParseException {
        String jwtToken = requestBuilderService.getValidatedToken(dcKeyCloakUrl, dcClientId, dcClientSecret);
        model.addAttribute("sftp_post_endpoint_url", sftpPostEndpointUrl);
        model.addAttribute("sftp_dc_data_endpoint_url", sftpDcDataEndpointUrl);
        model.addAttribute("sftp_dp1_data_endpoint_url", sftpDp1DataEndpointUrl);
        model.addAttribute("sftp_dp2_data_endpoint_url", sftpDp2DataEndpointUrl);
        model.addAttribute("clear_dc_db_endpoint_url", clearDcDbEndpointUrl);
        model.addAttribute("jwtToken", jwtToken);
        return "dashboardSftpSse";
    }
}
