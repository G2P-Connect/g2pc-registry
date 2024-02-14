package g2pc.dc.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.search.message.response.ResponseDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseDTO;
import g2pc.core.lib.exceptions.G2pcError;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TxnTrackerService {

    void saveInitialTransaction(List<Map<String, Object>> payloadMapList, String transactionId, String status, String protocol) throws JsonProcessingException;

    void saveRequestTransaction(String requestString, String regType, String transactionId, String protocol) throws JsonProcessingException;

    CacheDTO createCache(String data, String status, String protocol);

    void saveCache(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException;

    G2pcError saveRequestInDB(String requestString, String regType, String protocol,
                              G2pcError g2pcError, String payloadFilename,
                              String inboundFilename, Boolean sunbirdEnabled) throws IOException;

    G2pcError updateTransactionDbAndCache(ResponseDTO responseDTO, String outboundFilename, Boolean sunbirdEnabled) throws IOException;

    void saveInitialStatusTransaction(String txnType, String transactionId, String status, String protocol) throws JsonProcessingException;

    G2pcError saveRequestInStatusDB(String requestString, String regType) throws IOException;

    G2pcError updateStatusTransactionDbAndCache(StatusResponseDTO statusResponseDTO) throws IOException;

}
