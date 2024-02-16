package g2pc.core.lib.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.security.G2pTokenResponse;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import kong.unirest.UnirestException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

@Service
public interface G2pTokenService {

    public G2pTokenResponse getToken(String URL  ,String clientId, String clientSecret) throws IOException, UnirestException;


    public TokenExpiryDto createTokenExpiryDto(G2pTokenResponse g2pTokenResponse);

    public Boolean isTokenExpired(TokenExpiryDto tokenExpiryDto) throws ParseException;

    public ArrayList<Map<String, String>> getClientByRealm(String masterAdminUrl, String getClientUrl , String clientId , String clientSecret
            , String username , String password) throws JsonProcessingException;

    public boolean validateToken(String masterAdminUrl, String getClientUrl , String clientId ,
                                 String adminClientId , String adminClientSecret
            , String username , String password) throws JsonProcessingException;

    public String decodeToken(String token) throws JsonProcessingException;

    public ResponseEntity<String> getInterSpectResponse(String url, String token, String clientId, String clientSecret) throws UnirestException;
}
