package g2pc.dp.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.dto.common.message.response.SearchResponseDTO;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.dp.core.lib.entity.MsgTrackerEntity;
import g2pc.dp.core.lib.entity.TxnTrackerEntity;
import kong.unirest.UnirestException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface ResponseBuilderService {

    ResponseMessageDTO buildResponseMessage(String transactionId, List<SearchResponseDTO> searchResponseDTOList);

    ResponseHeaderDTO getResponseHeaderDTO(MsgTrackerEntity msgTrackerEntity);

    String buildResponseString(String signatureString, ResponseHeaderDTO responseHeaderDTO, ResponseMessageDTO messageDTO) throws JsonProcessingException;

    G2pcError sendOnSearchResponse(String responseString, String uri, String clientId, String clientSecret, String keyClockClientTokenUrl) throws Exception;

    public void saveToken(String cacheKey, TokenExpiryDto tokenExpiryDto) throws JsonProcessingException;

    public TokenExpiryDto getTokenFromCache(String clientId) throws JsonProcessingException;

    public String getValidatedToken(String keyCloakUrl, String clientId, String clientSecret) throws IOException, UnirestException, ParseException;
}
