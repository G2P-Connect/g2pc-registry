package g2pc.dc.core.lib;


import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.security.G2pTokenResponse;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import g2pc.core.lib.security.service.G2pTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class G2pcTokenServiceTest {


    @Autowired
    G2pTokenService g2pTokenService;


    @Value("${keycloak.from_dp.farmer.clientId}")
    private String clientId;

    @Value("${keycloak.from_dp.farmer.clientSecret}")
    private String clientSecret;

    @Value("${keycloak.from_dp.farmer.url}")
    private String keyCloakUrl;

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

    @Value("${keycloak.dp.master.getClientUrl}")
    private String getClientUrl;

    @Test
    public void testG2pcGetToken() throws Exception {
        G2pTokenResponse tokenResponse = g2pTokenService.getToken(keyCloakUrl, clientId, clientSecret);

        assertNotNull(tokenResponse);
        assertEquals("Bearer",tokenResponse.getTokenType());
        assertEquals("300",tokenResponse.getExpiresIn());
           }

    @Test
    public void testCreateTokenExpiryDto() throws Exception {

        G2pTokenResponse tokenResponse = new G2pTokenResponse("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJuSjAwNEloaVZabGpYQlBUWTBKWTdWdDhwemZaWVB0NTJOVUF1RElQam9rIn0.eyJleHAiOjE3MDgzMzY0NzQsImlhdCI6MTcwODMzNjE3NCwianRpIjoiYmUxOTE5NjctMDU2ZS00NDJhLThjNDktZTE4NGNjOTIzODFkIiwiaXNzIjoiaHR0cHM6Ly9nMnBjLWRwMS1sYWIuY2RwaS5kZXYvYXV0aC9yZWFsbXMvZHAtZmFybWVyIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImZkYjgzZWYwLWExNmItNDc1NC04NmJhLTU5OWMzYWFjZDY0OSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImRwLWZhcm1lci1jbGllbnQiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwiZGVmYXVsdC1yb2xlcy1kcC1mYXJtZXIiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImRwLWZhcm1lci1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiY2xpZW50SG9zdCI6IjEwNi4xOTUuOS4xOTYiLCJjbGllbnRJZCI6ImRwLWZhcm1lci1jbGllbnQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2aWNlLWFjY291bnQtZHAtZmFybWVyLWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMDYuMTk1LjkuMTk2In0.AI3YjQCcgNnoFviHkBJFSixLiAA2UHbF8-7tjek59OlPjIzhLFFUIyh4C_B_2_8rts0O9563eaGlhF-N3FsTjQY2ckHzzvzzBcoWoa8RjCglbp1v-AW-jB0GB-Vi3L9Vm4253pXzEeXe0_Qwv47dSY2IRpbzj_qXdSJZm6XfBdw4gq-AoYQPhSsOW-LqhrrhqKPvrVhUlIJELN3JUSOpl9_7uRS5p66GGRidhdL0Zd2BHWFwl6ADpVR1t1aAIh4AJxb1fON9kydVvxc1-Xrv6Gf2fc3ogDHlevzZUL4vs2cBObYXzej7l0mZH9Qj4aY5Lo8nqes1yzkmqik2jwKjiw","Bearer","300");
       TokenExpiryDto tokenExpiryDto =  g2pTokenService.createTokenExpiryDto(tokenResponse);
        assertNotNull(tokenExpiryDto);
    }

    @Test
    public void testTokenExpiry() throws Exception {

        G2pTokenResponse tokenResponse = new G2pTokenResponse("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJuSjAwNEloaVZabGpYQlBUWTBKWTdWdDhwemZaWVB0NTJOVUF1RElQam9rIn0.eyJleHAiOjE3MDgzMzY0NzQsImlhdCI6MTcwODMzNjE3NCwianRpIjoiYmUxOTE5NjctMDU2ZS00NDJhLThjNDktZTE4NGNjOTIzODFkIiwiaXNzIjoiaHR0cHM6Ly9nMnBjLWRwMS1sYWIuY2RwaS5kZXYvYXV0aC9yZWFsbXMvZHAtZmFybWVyIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImZkYjgzZWYwLWExNmItNDc1NC04NmJhLTU5OWMzYWFjZDY0OSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImRwLWZhcm1lci1jbGllbnQiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwiZGVmYXVsdC1yb2xlcy1kcC1mYXJtZXIiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImRwLWZhcm1lci1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiY2xpZW50SG9zdCI6IjEwNi4xOTUuOS4xOTYiLCJjbGllbnRJZCI6ImRwLWZhcm1lci1jbGllbnQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2aWNlLWFjY291bnQtZHAtZmFybWVyLWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMDYuMTk1LjkuMTk2In0.AI3YjQCcgNnoFviHkBJFSixLiAA2UHbF8-7tjek59OlPjIzhLFFUIyh4C_B_2_8rts0O9563eaGlhF-N3FsTjQY2ckHzzvzzBcoWoa8RjCglbp1v-AW-jB0GB-Vi3L9Vm4253pXzEeXe0_Qwv47dSY2IRpbzj_qXdSJZm6XfBdw4gq-AoYQPhSsOW-LqhrrhqKPvrVhUlIJELN3JUSOpl9_7uRS5p66GGRidhdL0Zd2BHWFwl6ADpVR1t1aAIh4AJxb1fON9kydVvxc1-Xrv6Gf2fc3ogDHlevzZUL4vs2cBObYXzej7l0mZH9Qj4aY5Lo8nqes1yzkmqik2jwKjiw","Bearer","300");
        TokenExpiryDto tokenExpiryDto =  g2pTokenService.createTokenExpiryDto(tokenResponse);
        assertFalse(g2pTokenService.isTokenExpired(tokenExpiryDto));
    }



    @Test
    public  void testValidateToken() throws JsonProcessingException {
        String token ="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJuSjAwNEloaVZabGpYQlBUWTBKWTdWdDhwemZaWVB0NTJOVUF1RElQam9rIn0.eyJleHAiOjE3MDgzMzY0NzQsImlhdCI6MTcwODMzNjE3NCwianRpIjoiYmUxOTE5NjctMDU2ZS00NDJhLThjNDktZTE4NGNjOTIzODFkIiwiaXNzIjoiaHR0cHM6Ly9nMnBjLWRwMS1sYWIuY2RwaS5kZXYvYXV0aC9yZWFsbXMvZHAtZmFybWVyIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImZkYjgzZWYwLWExNmItNDc1NC04NmJhLTU5OWMzYWFjZDY0OSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImRwLWZhcm1lci1jbGllbnQiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwiZGVmYXVsdC1yb2xlcy1kcC1mYXJtZXIiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImRwLWZhcm1lci1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiY2xpZW50SG9zdCI6IjEwNi4xOTUuOS4xOTYiLCJjbGllbnRJZCI6ImRwLWZhcm1lci1jbGllbnQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2aWNlLWFjY291bnQtZHAtZmFybWVyLWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMDYuMTk1LjkuMTk2In0.AI3YjQCcgNnoFviHkBJFSixLiAA2UHbF8-7tjek59OlPjIzhLFFUIyh4C_B_2_8rts0O9563eaGlhF-N3FsTjQY2ckHzzvzzBcoWoa8RjCglbp1v-AW-jB0GB-Vi3L9Vm4253pXzEeXe0_Qwv47dSY2IRpbzj_qXdSJZm6XfBdw4gq-AoYQPhSsOW-LqhrrhqKPvrVhUlIJELN3JUSOpl9_7uRS5p66GGRidhdL0Zd2BHWFwl6ADpVR1t1aAIh4AJxb1fON9kydVvxc1-Xrv6Gf2fc3ogDHlevzZUL4vs2cBObYXzej7l0mZH9Qj4aY5Lo8nqes1yzkmqik2jwKjiw";
        assertTrue(g2pTokenService.validateToken(this.masterUrl, this.getClientUrl, this.g2pTokenService.decodeToken(token), this.masterClientId, this.masterClientSecret, this.adminUsername, this.adminPassword));
    }
}
