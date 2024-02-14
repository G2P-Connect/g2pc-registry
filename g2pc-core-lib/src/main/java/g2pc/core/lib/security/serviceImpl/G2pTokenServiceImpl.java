package g2pc.core.lib.security.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.config.G2pUnirestHelper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.security.G2pTokenResponse;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import g2pc.core.lib.security.service.G2pTokenService;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Service contains methods related to security using token
 */
@Service
@Slf4j
public class G2pTokenServiceImpl implements G2pTokenService {


    @Autowired
    G2pUnirestHelper g2pUnirestHelper;

    public static String CLIENT_ID= "clientId";

    /**
     * Method to generate token
     * @param URL keycloak url
     * @param clientId clientID
     * @param clientSecret clientSecret
     * @return G2pTokenResponse
     * @throws IOException might be thrown 
     * @throws UnirestException might be thrown 
     */
    @Override
    public G2pTokenResponse getToken(String URL, String clientId, String clientSecret) throws IOException, UnirestException {

        String grantType = "client_credentials";
        ObjectMapper objectMapper = new ObjectMapper();
        // Make an HTTP POST request using Unirest
        HttpResponse<JsonNode> response = Unirest.post(URL)
                .header(CoreConstants.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .field(CoreConstants.GRANT_TYPE, grantType)
                .field(CoreConstants.CLIENT_ID, clientId)
                .field(CoreConstants.CLIENT_SECRET, clientSecret)
                .asJson();

        Map<String, Object> body = objectMapper.readValue(response.getBody().toString(), new TypeReference<Map<String, Object>>() {
        });
        G2pTokenResponse tokenResponse = new G2pTokenResponse();
        tokenResponse.setAccessToken(body.get(CoreConstants.ACCESS_TOKEN).toString());
        tokenResponse.setTokenType(body.get(CoreConstants.TOKEN_TYPE).toString());
        tokenResponse.setExpiresIn(body.get(CoreConstants.EXPIRES_IN).toString());
        return tokenResponse;
    }

    /**
     * Method to create tokenExpiryDto
     * @param g2pTokenResponse to create token expiry dto
     * @return tokenExpiryDto
     */
    @Override
    public TokenExpiryDto createTokenExpiryDto(G2pTokenResponse g2pTokenResponse) {
        TokenExpiryDto tokenExpiryDto = new TokenExpiryDto();
        tokenExpiryDto.setToken(g2pTokenResponse.getAccessToken());
        tokenExpiryDto.setExpiresIn(g2pTokenResponse.getExpiresIn());
        Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
        tokenExpiryDto.setDateSaved(currentTimeStamp);
        return tokenExpiryDto;
    }

    /**
     * Method to check whether token is expired or not by calculations
     * @param tokenExpiryDto to check token expired 
     * @return result 
     * @throws ParseException might be thrown 
     */
    @Override
    public Boolean isTokenExpired(TokenExpiryDto tokenExpiryDto) throws ParseException {
        if (tokenExpiryDto != null) {
            String lastSaved = tokenExpiryDto.getDateSaved().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            Date parsedDate = sdf.parse(lastSaved);
            Timestamp lastTimeStamp = new Timestamp(parsedDate.getTime());
            Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
            long milliseconds = currentTimeStamp.getTime() - lastTimeStamp.getTime();
            int seconds = (int) milliseconds / 1000;
            int minutes = seconds / 60;
            int expiry = Integer.parseInt(tokenExpiryDto.getExpiresIn()) / 60;
            return minutes > expiry;

        }
        return true;
    }

    /**
     * Method to return clients present in realm of keycloak to validate token
     * @param masterAdminUrl master admin url
     * @param getClientUrl get client url 
     * @return clients
     * @throws JsonProcessingException might be thrown 
     */
    @Override
    public ArrayList<Map<String, String>> getClientByRealm(String masterAdminUrl, String getClientUrl , String adminClientId , String adminClientSecret
            , String username , String password) throws JsonProcessingException {
        String grantType = "password";
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<Map<String, String>> responseMap = new ArrayList<>();
        HttpResponse<JsonNode> response = Unirest.post(masterAdminUrl)
                .header(CoreConstants.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .field(CoreConstants.GRANT_TYPE, grantType)
                .field(CoreConstants.CLIENT_ID, adminClientId)
                .field(CoreConstants.CLIENT_SECRET, adminClientSecret)
                .field( CoreConstants.USERNAME,username)
                .field(CoreConstants.PASSWORD,password)
                .asJson();
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody().toString(), new TypeReference<Map<String, Object>>() {});
        if(responseBody.get(CoreConstants.ACCESS_TOKEN)!=null){
            String token = responseBody.get(CoreConstants.ACCESS_TOKEN).toString();
            HttpResponse<String> clientResponse = g2pUnirestHelper.g2pGet(getClientUrl)
                    .header(CoreConstants.CONTENT_TYPE, "application/json")
                    .header(CoreConstants.AUTHORIZATION, "Bearer " + token)
                    .asString();
           responseMap  = objectMapper.readValue(clientResponse.getBody(), ArrayList.class);
        }
        return responseMap;

    }

    /**
     * Method to validate the token whether its present in client list of respective realm
     * @param masterAdminUrl keycloak master url
     * @param getClientUrl keycloak client url
     * @param clientId client id for validate
     * @return result
     * @throws JsonProcessingException might be thrown
     */
    @Override
    public boolean validateToken(String masterAdminUrl, String getClientUrl , String clientId ,
      String adminClientId , String adminClientSecret
            , String username , String password) throws JsonProcessingException {
        ArrayList<Map<String, String>> responseMap = getClientByRealm(masterAdminUrl, getClientUrl , adminClientId , adminClientSecret , username , password);
        boolean isValid = false;
        for (int i = 0; i < responseMap.size(); i++) {
            String responseClientId = responseMap.get(i).get("clientId");
            isValid = responseClientId.equals(clientId);
            if (isValid) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to decode the token
     * @param jwtToken token to decode
     * @return clientId
     * @throws JsonProcessingException might be thrown
     */
    @Override
    public String decodeToken(String jwtToken) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String[] splitString = jwtToken.split("\\.");
        String base64EncodedPayload = splitString[1];
        org.apache.commons.codec.binary.Base64 base64Url = new Base64(true);
        String body = new String(base64Url.decode(base64EncodedPayload));
        HashMap<String, String> payLoad = objectMapper.readValue(body, HashMap.class);
        return  payLoad.get(CLIENT_ID);
    }

    /**
     * Method to do introspect of token using keycloak api
     * @param url url for keycloak
     * @param token token to verify
     * @param clientId clientId to verify
     * @param clientSecret clientSecret to verify
     * @return result
     * @throws UnirestException might be thrown
     */
    @Override
    public ResponseEntity<String> getInterSpectResponse(String url, String token, String clientId, String clientSecret) throws UnirestException {
        HttpResponse<String> introResponse = Unirest.post(url)
                .header(CoreConstants.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .field("token", token)
                .field(CoreConstants.CLIENT_ID, clientId)
                .field(CoreConstants.CLIENT_SECRET, clientSecret)
                .asString();

        JSONObject json = new JSONObject(introResponse.getBody());
        if(json!=null && !json.getString("active").isEmpty()) {
            String isValid = json.getString("active");
            if (isValid.equals("true")) {
                return ResponseEntity.status(HttpStatus.OK).body("Token is valid");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is not valid");
    }
}