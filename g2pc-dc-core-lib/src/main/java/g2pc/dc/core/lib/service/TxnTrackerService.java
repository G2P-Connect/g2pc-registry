package g2pc.dc.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.dto.common.message.response.SearchResponseDTO;
import g2pc.dc.core.lib.entity.ResponseTrackerEntity;
import g2pc.dc.core.lib.repository.ResponseTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TxnTrackerService {

    void saveInitialTransaction(List<Map<String, Object>> payloadMapList, String transactionId, String status) throws JsonProcessingException;

    void saveRequestTransaction(String requestString, String regType, String transactionId) throws JsonProcessingException;

    CacheDTO createCache(String data, String status);

    void saveCache(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException;

    void saveRequestInDB(String requestString, String regType) throws JsonProcessingException;

    void updateTransactionDbAndCache(ResponseDTO responseDTO) throws JsonProcessingException;
}
