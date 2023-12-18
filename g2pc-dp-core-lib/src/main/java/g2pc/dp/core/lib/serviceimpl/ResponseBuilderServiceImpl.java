package g2pc.dp.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.config.G2pUnirestHelper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.MetaDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.common.message.response.*;
import g2pc.core.lib.dto.common.security.G2pTokenResponse;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import g2pc.core.lib.exceptionhandler.ErrorResponse;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.security.service.AsymmetricSignatureService;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dp.core.lib.entity.MsgTrackerEntity;
import g2pc.dp.core.lib.repository.MsgTrackerRepository;
import g2pc.dp.core.lib.service.ResponseBuilderService;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

@Service
@Slf4j
public class ResponseBuilderServiceImpl implements ResponseBuilderService {


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    G2pUnirestHelper g2pUnirestHelper;

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;

    @Autowired
    G2pTokenService g2pTokenService;

    @Value("${crypto.to_dc.support_encryption}")
    private boolean isEncrypt;

    @Value("${crypto.to_dc.support_signature}")
    private boolean isSign;

    @Value("${crypto.to_dc.password}")
    private String p12Password;
    @Autowired
    AsymmetricSignatureService asymmetricSignatureService;

    /**
     * Get response header
     *
     * @param msgTrackerEntity required
     * @return ResponseHeaderDTO
     */
    @Override
    public ResponseHeaderDTO getResponseHeaderDTO(MsgTrackerEntity msgTrackerEntity) {
        ResponseHeaderDTO headerDTO = new ResponseHeaderDTO();
        headerDTO.setVersion(msgTrackerEntity.getVersion());
        headerDTO.setMessageId(msgTrackerEntity.getMessageId());
        headerDTO.setMessageTs(msgTrackerEntity.getMessageTs());
        headerDTO.setAction(msgTrackerEntity.getAction());
        headerDTO.setSenderId(msgTrackerEntity.getSenderId());
        headerDTO.setReceiverId(msgTrackerEntity.getReceiverId());
        headerDTO.setIsMsgEncrypted(msgTrackerEntity.getIsMsgEncrypted());
        headerDTO.setStatus(msgTrackerEntity.getStatus());
        headerDTO.setStatusReasonCode(msgTrackerEntity.getStatusReasonCode());
        headerDTO.setStatusReasonMessage(msgTrackerEntity.getStatusReasonMessage());
        headerDTO.setTotalCount(msgTrackerEntity.getTotalCount());
        headerDTO.setCompletedCount(msgTrackerEntity.getCompletedCount());
        Map<String , Object > metaMap =  new HashMap<>();
        MetaDTO metaDTO = new MetaDTO();
        metaDTO.setData(metaMap);
        headerDTO.setMeta(metaDTO);
        return headerDTO;
    }

    /**
     * Build a response message
     *
     * @param transactionId         required
     * @param searchResponseDTOList required
     * @return ResponseMessageDTO
     */
    @Override
    public ResponseMessageDTO buildResponseMessage(String transactionId, List<SearchResponseDTO> searchResponseDTOList) {
        ResponseMessageDTO messageDTO = new ResponseMessageDTO();
        messageDTO.setTransactionId(transactionId);
        messageDTO.setCorrelationId(CommonUtils.generateUniqueId("C"));
        messageDTO.setSearchResponse(searchResponseDTOList);
        return messageDTO;
    }

    /**
     * Build a response string
     *
     * @param signatureString   required
     * @param responseHeaderDTO required
     * @param messageDTO        required
     * @return response String
     */
    @Override
    public String buildResponseString(String signatureString, ResponseHeaderDTO responseHeaderDTO, ResponseMessageDTO messageDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setSignature(signatureString);
        responseDTO.setHeader(responseHeaderDTO);
        responseDTO.setMessage(messageDTO);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseDTO);
    }

    @Override
    public G2pcError sendOnSearchResponse(String responseString, String uri, String clientId, String clientSecret, String keyClockClientTokenUrl
            , InputStream fis , String encryptedSalt) throws Exception {
        log.info("Send on-search response");
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("Is encrypted ? -> "+isEncrypt);
        log.info("Is signed ? -> "+isSign);
        responseString = createSignature( isEncrypt, isSign , responseString , fis , encryptedSalt);
        String jwtToken = getValidatedToken(keyClockClientTokenUrl, clientId, clientSecret);
        HttpResponse<String> response = g2pUnirestHelper.g2pPost(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .body(responseString)
                .asString();
        log.info("on-search response status = {}", response.getStatus());
        if (response.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            G2pcError g2pcError = new G2pcError(HttpStatus.INTERNAL_SERVER_ERROR.toString(), response.getBody());
            return g2pcError;
        } else if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            G2pcError g2pcError = new G2pcError( HttpStatus.UNAUTHORIZED.toString(), response.getBody());
            return g2pcError;
        } else if (response.getStatus() == HttpStatus.BAD_REQUEST.value()) {
            G2pcError g2pcError = new G2pcError(HttpStatus.BAD_REQUEST.toString(), response.getBody());
            return g2pcError;
        } else if (response.getStatus() != HttpStatus.OK.value()) {
            G2pcError g2pcError = new G2pcError("err.service.unavailable", response.getBody());
            return g2pcError;
        }
        G2pcError g2pcError = new G2pcError(HttpStatus.OK.toString(), response.getBody());
        return g2pcError;
    }

    /**
     * Method to store token in cache
     *
     * @param cacheKey
     * @param tokenExpiryDto
     * @throws JsonProcessingException
     */
    @Override
    public void saveToken(String cacheKey, TokenExpiryDto tokenExpiryDto) throws JsonProcessingException {

        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(tokenExpiryDto));
    }

    /**
     * Method to get token stored in cache
     *
     * @param clientId
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public TokenExpiryDto getTokenFromCache(String clientId) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Set<String> redisKeys = this.redisTemplate.keys(clientId);
        List<String> cacheKeysList = new ArrayList((Collection) Objects.requireNonNull(redisKeys));
        if (!cacheKeysList.isEmpty()) {
            String cacheKey = cacheKeysList.get(0);
            String tokenData = (String) this.redisTemplate.opsForValue().get(cacheKey);
            TokenExpiryDto tokenExpiryDto = objectMapper.readerFor(TokenExpiryDto.class).readValue(tokenData);
            return tokenExpiryDto;
        }
        return null;
    }

    /**
     * The method to get validated token
     *
     * @param keyCloakUrl
     * @param clientId
     * @param clientSecret
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws UnirestException
     */
    @Override
    public String getValidatedToken(String keyCloakUrl, String clientId, String clientSecret) throws IOException, ParseException, UnirestException {
        TokenExpiryDto tokenExpiryDto = getTokenFromCache(clientId);

        String jwtToken = "";
        if (g2pTokenService.isTokenExpired(tokenExpiryDto)) {
            G2pTokenResponse tokenResponse = g2pTokenService.getToken(keyCloakUrl, clientId, clientSecret);
            jwtToken = tokenResponse.getAccess_token();
            saveToken(clientId, g2pTokenService.createTokenExpiryDto(tokenResponse));
        } else {
            jwtToken = tokenExpiryDto.getToken();
        }
        return jwtToken;
    }

    /**
     * The method is to create signature
     *
     * @param isSign
     * @param isEncrypt
     * @param responseString
     * @return
     * @throws Exception
     */
    private String createSignature( boolean isEncrypt, boolean isSign, String responseString
            , InputStream fis , String encryptedSalt) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);
        ResponseDTO responseDTO = objectMapper.readerFor(ResponseDTO.class).
                readValue(responseString);

        byte[] json = objectMapper.writeValueAsBytes( responseDTO.getMessage());
        ResponseMessageDTO messageDTO =  objectMapper.readValue(json, ResponseMessageDTO.class);
        ResponseHeaderDTO responseHeaderDTO = (ResponseHeaderDTO) responseDTO.getHeader();
        String responseHeaderString = objectMapper.writeValueAsString(responseHeaderDTO);
        String messageString = objectMapper.writeValueAsString(messageDTO);
        String signature = null;

        if(isSign){
            if(isEncrypt){
                String encryptedMessageString = encryptDecrypt.g2pEncrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                responseDTO.setMessage(encryptedSalt+encryptedMessageString);
                responseDTO.getHeader().setIsMsgEncrypted(true);
                Map<String , Object> meta= (Map<String, Object>) responseDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN,true);
                responseDTO.getHeader().getMeta().setData(meta);
                responseHeaderString = objectMapper.writeValueAsString(responseDTO.getHeader());
                byte[] asymmetricSignature = asymmetricSignatureService.sign( responseHeaderString+encryptedMessageString ,fis ,p12Password);
                signature = Base64.getEncoder().encodeToString(asymmetricSignature);
                log.info("Encrypted message ->"+encryptedMessageString);
                log.info("Hashed Signature ->"+signature);
            }else{
                responseDTO.getHeader().setIsMsgEncrypted(false);
                Map<String , Object> meta= (Map<String, Object>) responseDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN,true);
                responseDTO.getHeader().getMeta().setData(meta);
                responseHeaderString = objectMapper.writeValueAsString(responseDTO.getHeader());
                byte[] asymmetricSignature = asymmetricSignatureService.sign( responseHeaderString+messageString,fis,p12Password);
                signature = Base64.getEncoder().encodeToString(asymmetricSignature);
                log.info("Hashed Signature ->"+signature);
            }
        } else {
            if(isEncrypt){
                String encryptedMessageString = encryptDecrypt.g2pEncrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                responseDTO.setMessage(encryptedSalt+encryptedMessageString);
                responseDTO.getHeader().setIsMsgEncrypted(true);
                Map<String , Object> meta= (Map<String, Object>) responseDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN,false);
                responseDTO.getHeader().getMeta().setData(meta);
                log.info("Encrypted message ->"+encryptedMessageString);
            }else{
                responseDTO.getHeader().setIsMsgEncrypted(false);
                Map<String , Object> meta= (Map<String, Object>) responseDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN,false);
                responseDTO.getHeader().getMeta().setData(meta);
            }
        }
        responseDTO.setSignature(signature);
        responseString = objectMapper.writeValueAsString(responseDTO);
        return responseString;
    }
}
