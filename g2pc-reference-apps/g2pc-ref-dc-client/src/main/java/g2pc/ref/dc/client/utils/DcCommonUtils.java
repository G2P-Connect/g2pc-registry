package g2pc.ref.dc.client.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.security.BearerTokenUtil;
import g2pc.core.lib.security.service.G2pTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private String masterClientId;

    @Value("${keycloak.dc.client.clientSecret}")
    private String masterClientSecret;

    @Value("${keycloak.dc.username}")
    private String adminUsername;

    @Value("${keycloak.dc.password}")
    private String adminPassword;

    public void handleToken() throws G2pHttpException, JsonProcessingException {
        String token = BearerTokenUtil.getBearerTokenHeader();
        String introspect = keycloakURL + "/introspect";
        ResponseEntity<String> introspectResponse = g2pTokenService.getInterSpectResponse(introspect, token, masterClientId, masterClientSecret);
        if (introspectResponse.getStatusCode().value() == 401) {
            throw new G2pHttpException(new G2pcError(introspectResponse.getStatusCode().toString(), introspectResponse.getBody()));
        }
        if (!g2pTokenService.validateToken(masterUrl, getClientUrl, g2pTokenService.decodeToken(token), masterClientId, masterClientSecret, adminUsername, adminPassword)) {
            //TODO:check this
            //throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_USER_UNAUTHORIZED.toValue(), "User is not authorized"));
        }
    }
}
