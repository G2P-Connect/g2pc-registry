package g2pc.ref.dc.client.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.constants.SftpConstants;
import g2pc.core.lib.dto.sftp.SftpServerConfigDTO;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.security.BearerTokenUtil;
import g2pc.core.lib.security.service.G2pTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class DcCommonUtils {

    @Autowired
    G2pTokenService g2pTokenService;

    @Value("${keycloak.dc.client.realm}")
    private String keycloakRealm;

    @Value("${keycloak.dc.client.url}")
    private String keycloakURL;

    @Value("${keycloak.dc.master.url}")
    private String masterUrl;

    @Value("${keycloak.dc.master.getClientUrl}")
    private String getClientUrl;

    @Value("${keycloak.dc.client.clientId}")
    private String dcClientId;

    @Value("${keycloak.dc.client.clientSecret}")
    private String dcClientSecret;

    @Value("${keycloak.dc.master.clientId}")
    private String masterClientId;

    @Value("${keycloak.dc.master.clientSecret}")
    private String masterClientSecret;

    @Value("${keycloak.dc.username}")
    private String adminUsername;

    @Value("${keycloak.dc.password}")
    private String adminPassword;

    @Value("${sftp.listener.host}")
    private String sftpDcHost;

    @Value("${sftp.listener.port}")
    private int sftpDcPort;

    @Value("${sftp.listener.user}")
    private String sftpDcUser;

    @Value("${sftp.listener.password}")
    private String sftpDcPassword;

    @Value("${sftp.listener.remote.inbound_directory}")
    private String sftpDcRemoteInboundDirectory;

    @Value("${sftp.listener.remote.outbound_directory}")
    private String sftpDcRemoteOutboundDirectory;

    @Value("${sftp.listener.local.inbound_directory}")
    private String sftpDcLocalInboundDirectory;

    @Value("${sftp.listener.local.outbound_directory}")
    private String sftpDcLocalOutboundDirectory;

    public void handleToken() throws G2pHttpException, JsonProcessingException {
        String token = BearerTokenUtil.getBearerTokenHeader();
        String introspect = keycloakURL + "/introspect";
        ResponseEntity<String> introspectResponse = g2pTokenService.getInterSpectResponse(introspect, token, dcClientId, dcClientSecret);
        if (introspectResponse.getStatusCode().value() == 401) {
            throw new G2pHttpException(new G2pcError(introspectResponse.getStatusCode().toString(), introspectResponse.getBody()));
        }
        if (!g2pTokenService.validateToken(masterUrl, getClientUrl, g2pTokenService.decodeToken(token),
                masterClientId, masterClientSecret, adminUsername, adminPassword)) {
            throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_USER_UNAUTHORIZED.toValue(), "User is not authorized"));
        }
    }

    public SftpServerConfigDTO getSftpConfigForDc() {
        SftpServerConfigDTO sftpServerConfigDTO = new SftpServerConfigDTO();
        sftpServerConfigDTO.setHost(sftpDcHost);
        sftpServerConfigDTO.setPort(sftpDcPort);
        sftpServerConfigDTO.setUser(sftpDcUser);
        sftpServerConfigDTO.setPassword(sftpDcPassword);
        sftpServerConfigDTO.setAllowUnknownKeys(true);
        sftpServerConfigDTO.setStrictHostKeyChecking("no");
        sftpServerConfigDTO.setRemoteInboundDirectory(sftpDcRemoteInboundDirectory);
        sftpServerConfigDTO.setRemoteOutboundDirectory(sftpDcRemoteOutboundDirectory);
        sftpServerConfigDTO.setLocalInboundDirectory(sftpDcLocalInboundDirectory);
        sftpServerConfigDTO.setLocalOutboundDirectory(sftpDcLocalOutboundDirectory);
        return sftpServerConfigDTO;
    }

     public void deleteFolder(Path path) {
        try {
            if (Files.isRegularFile(path)) {
                Files.delete(path);
                return;
            }
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(p -> p.compareTo(path) != 0).forEach(p -> deleteFolder(p)); // delete all the children folders or files;
                Files.delete(path); // delete the folder itself;
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }
}
