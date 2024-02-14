package g2pc.dp.core.lib.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.sftp.SftpServerConfigDTO;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.security.BearerTokenUtil;
import g2pc.core.lib.security.service.G2pTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DpCommonUtils {

    @Value("${keycloak.dp.client.realm}")
    private String keycloakRealm;

    @Value("${keycloak.dp.master.getClientUrl}")
    private String getClientUrl;

    @Value("${crypto.to_dc.support_encryption}")
    private boolean isEncrypt;

    @Value("${crypto.to_dc.support_signature}")
    private boolean isSign;

    @Value("${keycloak.dp.client.url}")
    private String keycloakURL;

    @Value("${keycloak.dp.client.clientId}")
    private String keycloakClientId;

    @Value("${keycloak.dp.client.clientSecret}")
    private String keycloakClientSecret;

    @Value("${keycloak.dp.master.url}")
    private String masterUrl;

    @Value("${keycloak.dp.master.clientId}")
    private String masterClientId;

    @Value("${keycloak.dp.master.clientSecret}")
    private String masterClientSecret;

    @Value("${keycloak.dp.username}")
    private String adminUsername;

    @Value("${keycloak.dp.password}")
    private String adminPassword;

    @Autowired
    G2pTokenService g2pTokenService;

    @Value("${sftp.dc.host}")
    private String sftpDcHost;

    @Value("${sftp.dc.port}")
    private int sftpDcPort;

    @Value("${sftp.dc.user}")
    private String sftpDcUser;

    @Value("${sftp.dc.password}")
    private String sftpDcPassword;

    @Value("${sftp.dc.remote.outbound_directory}")
    private String sftpDcRemoteOutboundDirectory;


    public void handleToken() throws G2pHttpException, JsonProcessingException {
        log.info("Is encrypted ? -> " + isEncrypt);
        log.info("Is signed ? -> " + isSign);
        String token = BearerTokenUtil.getBearerTokenHeader();
        String introspectUrl = keycloakURL + "/introspect";
        ResponseEntity<String> introspectResponse = g2pTokenService.getInterSpectResponse(introspectUrl, token,
                keycloakClientId, keycloakClientSecret);
        log.info("Introspect response -> " + introspectResponse.getStatusCode());
        log.info("Introspect response body -> " + introspectResponse.getBody());
        if (introspectResponse.getStatusCode().value() == 401) {
            throw new G2pHttpException(new G2pcError(introspectResponse.getStatusCode().toString(), introspectResponse.getBody()));
        }
        if (!g2pTokenService.validateToken(masterUrl, getClientUrl,
                g2pTokenService.decodeToken(token), masterClientId, masterClientSecret,
                adminUsername, adminPassword)) {
            throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_USER_UNAUTHORIZED.toValue(), "User is not authorized"));
        }
    }

    public SftpServerConfigDTO getSftpConfigForDp() {
        SftpServerConfigDTO sftpServerConfigDTO = new SftpServerConfigDTO();
        sftpServerConfigDTO.setHost(sftpDcHost);
        sftpServerConfigDTO.setPort(sftpDcPort);
        sftpServerConfigDTO.setUser(sftpDcUser);
        sftpServerConfigDTO.setPassword(sftpDcPassword);
        sftpServerConfigDTO.setAllowUnknownKeys(true);
        sftpServerConfigDTO.setStrictHostKeyChecking("no");
        sftpServerConfigDTO.setRemoteOutboundDirectory(sftpDcRemoteOutboundDirectory);
        return sftpServerConfigDTO;
    }
}
