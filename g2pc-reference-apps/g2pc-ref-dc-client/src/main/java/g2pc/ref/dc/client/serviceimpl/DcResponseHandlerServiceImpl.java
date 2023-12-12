package g2pc.ref.dc.client.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.ResponseHandlerService;
import g2pc.dc.core.lib.service.TxnTrackerService;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.repository.RegistryTransactionsRepository;
import g2pc.ref.dc.client.service.DcResponseHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class DcResponseHandlerServiceImpl implements DcResponseHandlerService {

    @Autowired
    private TxnTrackerService txnTrackerService;

    @Override
    public AcknowledgementDTO getResponse(ResponseDTO responseDTO) throws JsonProcessingException {
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        ObjectMapper objectMapper = new ObjectMapper();

        txnTrackerService.updateTransactionDbAndCache(responseDTO);
        log.info("on-search response received from registry : {}", objectMapper.writeValueAsString(responseDTO));
        acknowledgementDTO.setMessage(Constants.ON_SEARCH_RESPONSE_RECEIVED);
        acknowledgementDTO.setStatus(Constants.COMPLETED);
        return acknowledgementDTO;
    }
}
