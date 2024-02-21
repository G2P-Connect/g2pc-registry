package g2pc.dc.core.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import g2pc.core.lib.dto.search.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.search.message.request.SearchCriteriaDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestDTO;
import g2pc.core.lib.dto.status.message.request.TxnStatusRequestDTO;
import g2pc.core.lib.enums.ActionsENUM;
import g2pc.core.lib.enums.StatusTransactionTypeEnum;
import g2pc.core.lib.security.service.AsymmetricSignatureService;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.RequestBuilderService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import g2pc.core.lib.security.serviceImpl.G2pEncryptDecryptImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@SpringBootTest
class RequestBuilderServiceTests {

    private static final Logger log = LoggerFactory.getLogger(RequestBuilderServiceTests.class);
    @Autowired
    G2pEncryptDecrypt encryptDecrypt;

    @Autowired
    AsymmetricSignatureService asymmetricSignatureService;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    RequestBuilderService requestBuilderService;

    @Autowired
    G2pTokenService g2pTokenService;

    @Value("${keycloak.from_dp.farmer.clientId}")
    private String clientId;

    @Value("${keycloak.from_dp.farmer.clientSecret}")
    private String clientSecret;

    @Value("${keycloak.from_dp.farmer.url}")
    private String keyCloakUrl;

    @Disabled
    @Test
    void contextLoads() {
    }

    @Disabled
    @Test
    void testG2pEncrypt() throws Exception {
        String data = "Hello, World!";
        String key = G2pSecurityConstants.SECRET_KEY;
        encryptDecrypt = new G2pEncryptDecryptImpl();
        data = "Hello, World!";
        String result = encryptDecrypt.g2pEncrypt(data, key);
        String expectedResult = encryptDecrypt.g2pDecrypt(result, key);
        assertEquals(expectedResult, data);
    }

    @Disabled
    @Test
    void testNegativeG2pEncrypt() throws Exception {
        String data = "Hello, World!";
        String key = G2pSecurityConstants.SECRET_KEY;
        data = "Hello, World!";
        String result = encryptDecrypt.g2pEncrypt(data, key);
        String expectedResult = encryptDecrypt.g2pDecrypt(result, key);
        assertNotEquals(expectedResult, data + "salt");
    }

    @Disabled
    @Test
    void testAsymmetricSignature() throws Exception {
        String data = "Hello, World!";
        String password = "test";
        byte[] signatureBytes = null;
        try {
            resourceLoader = new DefaultResourceLoader();
            Resource resource = resourceLoader.getResource("classpath:1693731657.p12");
            InputStream fis = resource.getInputStream(); // Replace with actual private key content
            signatureBytes = asymmetricSignatureService.sign(data, fis, password);
            String signature = Base64.getEncoder().encodeToString(signatureBytes);
            log.info(signature + "");
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        assertNotNull(signatureBytes);
        assertTrue(signatureBytes.length > 0);
    }

    @Disabled
    @Test
    void testVerifySign() throws Exception {
        String data = "Hello, World!";
        String password = "test";
        String signature = "FHBtv9grNxO46BHWIcgRy3LFHcnYo2iHcRCnh5cVhV/nNoD2k8NM17T4ogwBKOpSImkHkmh2+uqgseDdl07WlBSRrj7cqOUiQWPjyQH1oox6mJRRnhRjncBST+JHDDg/ufmyQmRNMrCtYEdWk0gM6BFDeosUtM8wR1YRPDpy34KUwk7IUU4dHMtyqAQZTVo5qWdyq1mU3VUt1baShUBQnM5P0kyI3j4S1KDIFakrCl2uPmygZqUR8udM0ha0CkhpZ4nXi+KfzDojPL3Cv6PKnO3FEROn4hedbUdxv31OjzKIGJZ8H3LcMYUpwBfmVIvrm9SBsBspAZNghod8RLGB/2Qv4q/Co5CF8+SXVY/PeoAdUy92hKpim/lxOj1NWoiSVNAHD8i4w4pyAqptJ1tUzS6hQ+5lKto4FI2dCdOgs0WSFrHjC8UFO+7sU2182ulST1yhcUJ450ItWnTmC9/O248Fliz27cioItHRybUyYaRFARRanmdJPMj/WiwyUwNuIRDvqgHHd1Nlp/UGeSHjYXwlEfrby0TaQD2/FeA5pGcxcWd2p+cSrnJSuO+KkZOWj5xrvtEZrI9x/5dapA7aEZeY0T+s24MudeH1QT7gxqgZ4QHtZRHR2BPatZ9Yhqr5p4YoBrJDc88KJBsuqg+IotZ0iHsFnR3tdzXNnNCwl+o=";

        Resource resource = resourceLoader.getResource("classpath:1693731657.p12");
        InputStream fis = resource.getInputStream();
        assertTrue(asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(signature), fis, password));

    }

    @Disabled
    @Test
    void testCreateSignature() {
        String requestString = "{\n" +
                "  \"signature\" : \"new signature to be generated for request\",\n" +
                "  \"header\" : {\n" +
                "    \"type\" : \"requestHeader\",\n" +
                "    \"version\" : \"1.0.0\",\n" +
                "    \"action\" : \"search\",\n" +
                "    \"meta\" : {\n" +
                "      \"data\" : { }\n" +
                "    },\n" +
                "    \"message_id\" : \"M771-9507-0471-2746-9172\",\n" +
                "    \"message_ts\" : \"2024-02-19T11:50:49+05:30\",\n" +
                "    \"total_count\" : 21800,\n" +
                "    \"sender_id\" : \"spp.example.org\",\n" +
                "    \"receiver_id\" : \"pymts.example.org\",\n" +
                "    \"is_msg_encrypted\" : false,\n" +
                "    \"sender_uri\" : \"https://spp.example.org/{namespace}/callback/on-search\"\n" +
                "  },\n" +
                "  \"message\" : {\n" +
                "    \"transaction_id\" : \"T223-7988-8811-8349-3288\",\n" +
                "    \"search_request\" : [ {\n" +
                "      \"reference_id\" : \"R205-0067-1389-3560-4002\",\n" +
                "      \"timestamp\" : \"2024-02-19T11:50:49+05:30\",\n" +
                "      \"search_criteria\" : {\n" +
                "        \"version\" : \"1.0.0\",\n" +
                "        \"reg_type\" : \"ns:MOBILE_REGISTRY\",\n" +
                "        \"reg_sub_type\" : \"\",\n" +
                "        \"query_type\" : \"namedQuery\",\n" +
                "        \"query\" : {\n" +
                "          \"query_name\" : \"mobile_registered\",\n" +
                "          \"query_params\" : {\n" +
                "            \"season\" : \"2023-xyz\",\n" +
                "            \"mobile_number\" : \"9767670151\",\n" +
                "            \"status\" : \"\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"sort\" : [ {\n" +
                "          \"attribute_name\" : \"mobile_number\",\n" +
                "          \"sort_order\" : \"asc\"\n" +
                "        } ],\n" +
                "        \"pagination\" : {\n" +
                "          \"page_size\" : 10,\n" +
                "          \"page_number\" : 1\n" +
                "        },\n" +
                "        \"consent\" : {\n" +
                "          \"ts\" : null,\n" +
                "          \"purpose\" : null\n" +
                "        },\n" +
                "        \"authorize\" : {\n" +
                "          \"ts\" : null,\n" +
                "          \"purpose\" : null\n" +
                "        }\n" +
                "      },\n" +
                "      \"locale\" : \"en\"\n" +
                "    }, {\n" +
                "      \"reference_id\" : \"R516-9061-3725-7595-7274\",\n" +
                "      \"timestamp\" : \"2024-02-19T11:50:49+05:30\",\n" +
                "      \"search_criteria\" : {\n" +
                "        \"version\" : \"1.0.0\",\n" +
                "        \"reg_type\" : \"ns:MOBILE_REGISTRY\",\n" +
                "        \"reg_sub_type\" : \"\",\n" +
                "        \"query_type\" : \"namedQuery\",\n" +
                "        \"query\" : {\n" +
                "          \"query_name\" : \"mobile_registered\",\n" +
                "          \"query_params\" : {\n" +
                "            \"season\" : \"2023-xyz\",\n" +
                "            \"mobile_number\" : \"9767670152\",\n" +
                "            \"status\" : \"\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"sort\" : [ {\n" +
                "          \"attribute_name\" : \"mobile_number\",\n" +
                "          \"sort_order\" : \"asc\"\n" +
                "        } ],\n" +
                "        \"pagination\" : {\n" +
                "          \"page_size\" : 10,\n" +
                "          \"page_number\" : 1\n" +
                "        },\n" +
                "        \"consent\" : {\n" +
                "          \"ts\" : null,\n" +
                "          \"purpose\" : null\n" +
                "        },\n" +
                "        \"authorize\" : {\n" +
                "          \"ts\" : null,\n" +
                "          \"purpose\" : null\n" +
                "        }\n" +
                "      },\n" +
                "      \"locale\" : \"en\"\n" +
                "    } ]\n" +
                "  }\n" +
                "}";
        String encryptionSalt = "";
        String p12Password = "test";
        String txnType = "type";

        try {
            resourceLoader = new DefaultResourceLoader();
            Resource resource = resourceLoader.getResource("classpath:1693731657.p12");
            InputStream fis = resource.getInputStream(); // Replace with actual private key content
            String signature = requestBuilderService.createSignature(true, true,
                    requestString, fis, encryptionSalt, p12Password, txnType);
            assertNotNull(signature);
            assertTrue(!signature.isEmpty());
            log.info("Signature: " + signature);

        } catch (Exception e) {
            //fail("Exception thrown: " + e.getMessage());
        }
    }

    @Disabled
    @Test
    void testValidatedToken() throws IOException, ParseException {
        String token = requestBuilderService.getValidatedToken(keyCloakUrl, clientId, clientSecret);
        assertNotNull(token);
        String resultclientId = g2pTokenService.decodeToken(token);
        assertEquals(resultclientId, clientId);
    }

    @Disabled
    @Test
    void testGetTokenFromCache() throws IOException, ParseException {
        TokenExpiryDto token = requestBuilderService.getTokenFromCache(clientId + "-token");
        assertNotNull(token.getToken());
        String resultClientId = g2pTokenService.decodeToken(token.getToken());
        assertEquals(resultClientId, clientId);
    }

    @Disabled
    @Test
    void testBuildStatusRequest() throws JsonProcessingException {
        TxnStatusRequestDTO txnStatusRequestDTO = requestBuilderService.buildTransactionRequest("TransactionId", StatusTransactionTypeEnum.SEARCH.toValue());
        String statusRequestString = requestBuilderService.buildStatusRequest(txnStatusRequestDTO, "TransactionId", ActionsENUM.STATUS);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);
        StatusRequestDTO requestDTO = objectMapper.readerFor(StatusRequestDTO.class).
                readValue(statusRequestString);
        assertNotNull(requestDTO);
    }

    @Disabled
    @Test
    void testGeneratePayloadFromCsv() throws JsonProcessingException {
        File payloadFile = new File("inputfiles/requestbuilder/payload.csv");
        List<Map<String, Object>> result = requestBuilderService.generatePayloadFromCsv(payloadFile);
        assertNotNull(result);
        log.info("Result : {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }

    @Disabled
    @Test
    void testCreateQueryMap() throws Exception {

        String payloadMapString = readJsonFile("payloadMap.json");

        String entrySetString = readJsonFile("entrySet.json");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> payloadMapList = objectMapper.readValue(payloadMapString, new TypeReference<List<Map<String, Object>>>() {
        });
        Set<Map.Entry<String, Object>> entrySet = objectMapper.readValue(entrySetString, new TypeReference<Set<Map.Entry<String, Object>>>() {
        });
        List<Map<String, Object>> result = requestBuilderService.createQueryMap(payloadMapList, entrySet);
        assertNotNull(result);
        log.info("Result : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }

    @Disabled
    @Test
    void testGetSearchCriteriaDTO() throws IOException {
        String queryParamsMapString = readJsonFile("queryParamsMap.json");
        String registrySpecificConfigMapString = readJsonFile("registrySpecificConfigMap.json");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> queryParamsMap = objectMapper.readValue(queryParamsMapString, new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> registrySpecificConfigMap = objectMapper.readValue(registrySpecificConfigMapString, new TypeReference<Map<String, Object>>() {
        });
        SearchCriteriaDTO searchCriteriaDTO = requestBuilderService.getSearchCriteriaDTO(queryParamsMap, registrySpecificConfigMap);
        assertNotNull(searchCriteriaDTO);
        log.info("Result : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(searchCriteriaDTO));
    }

    @Disabled
    @Test
    void testBuildMessage() throws IOException {
        String searchCriteriaDTOListString = readJsonFile("searchCriteriaDTOList.json");
        ObjectMapper objectMapper = new ObjectMapper();
        RequestMessageDTO requestMessageDTO = requestBuilderService.buildMessage(objectMapper.readValue(searchCriteriaDTOListString, new TypeReference<List<SearchCriteriaDTO>>() {
        }));
        assertNotNull(requestMessageDTO);
        log.info("Result : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestMessageDTO));
    }

    @Test
    void testBuildHeader() throws JsonProcessingException {
        HeaderDTO headerDTO = requestBuilderService.buildHeader(ActionsENUM.SEARCH);
        assertNotNull(headerDTO);
        log.info("Result : {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(headerDTO));
    }

    @Disabled
    @Test
    void testBuildRequest() throws IOException {
        String searchCriteriaDTOListString = readJsonFile("searchCriteriaDTOList.json");
        ObjectMapper objectMapper = new ObjectMapper();

        String transactionId = CommonUtils.generateUniqueId("T");
        String result = requestBuilderService.buildRequest(objectMapper.readValue(searchCriteriaDTOListString, new TypeReference<List<SearchCriteriaDTO>>() {
        }), transactionId, ActionsENUM.SEARCH);
        assertNotNull(result);
        log.info("Result : {}", result);
    }

    public String readJsonFile(String filename) throws IOException {
        File file = new File("inputfiles/" + filename);
        return new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
    }
}