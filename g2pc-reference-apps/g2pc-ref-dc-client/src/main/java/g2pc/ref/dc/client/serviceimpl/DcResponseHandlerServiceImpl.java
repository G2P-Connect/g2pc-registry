package g2pc.ref.dc.client.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.dto.common.message.response.SearchResponseDTO;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.ResponseHandlerService;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.dto.farmer.response.RegRecordFarmerDTO;
import g2pc.ref.dc.client.dto.mobile.response.RegRecordMobileDTO;
import g2pc.ref.dc.client.entity.RegistryTransactionsEntity;
import g2pc.ref.dc.client.repository.RegistryTransactionsRepository;
import g2pc.ref.dc.client.service.DcResponseHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class DcResponseHandlerServiceImpl implements DcResponseHandlerService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RegistryTransactionsRepository registryTransactionsRepository;

    @Autowired
    private ResponseHandlerService responseHandlerService;

    @Override
    public void updateTransactionDbAndCache(String transactionId, SearchResponseDTO searchResponseDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String regType = searchResponseDTO.getData().getRegType();
        String cacheKey = "";
        Optional<RegistryTransactionsEntity> entityOptional = registryTransactionsRepository.getByTransactionId(transactionId);
        RegistryTransactionsEntity entity = new RegistryTransactionsEntity();
        if (entityOptional.isPresent()) {
            entity = entityOptional.get();
        }
        if (regType.equals("ns:FARMER_REGISTRY")) {
            log.info("on-search response received from farmer registry");
            cacheKey = "farmer-" + transactionId;

            DataDTO dataDTO = searchResponseDTO.getData();
            List<RegRecordFarmerDTO> recordFarmerDTOList = objectMapper.convertValue(dataDTO.getRegRecords(), new TypeReference<>() {
            });
            for (RegRecordFarmerDTO regRecordFarmerDTO : recordFarmerDTOList) {
                Optional<RegistryTransactionsEntity> optional = registryTransactionsRepository.getByTransactionIdAndFarmerId(transactionId, regRecordFarmerDTO.getFarmerId());
                if (optional.isEmpty() && (ObjectUtils.isNotEmpty(entity))) {
                    entity.setFarmerId(regRecordFarmerDTO.getFarmerId());
                    entity.setFarmerName(regRecordFarmerDTO.getFarmerName());
                    entity.setSeason(regRecordFarmerDTO.getSeason());
                    entity.setPaymentStatus(regRecordFarmerDTO.getPaymentStatus());
                    entity.setPaymentDate(regRecordFarmerDTO.getPaymentDate());
                    entity.setPaymentAmount(regRecordFarmerDTO.getPaymentAmount());
                    registryTransactionsRepository.save(entity);
                }
            }
        } else if (regType.equals("ns:MOBILE_REGISTRY")) {
            log.info("on-search response received from mobile registry");
            cacheKey = "mno-" + transactionId;

            DataDTO dataDTO = searchResponseDTO.getData();
            List<RegRecordMobileDTO> recordMobileDTOList = objectMapper.convertValue(dataDTO.getRegRecords(), new TypeReference<>() {
            });
            for (RegRecordMobileDTO regRecordMobileDTO : recordMobileDTOList) {
                Optional<RegistryTransactionsEntity> optional = registryTransactionsRepository.getByTransactionIdAndMobileNumber(transactionId, regRecordMobileDTO.getMobileNumber());
                if (optional.isEmpty() && (ObjectUtils.isNotEmpty(entity))) {
                    entity.setFarmerId(regRecordMobileDTO.getFarmerId());
                    entity.setFarmerName(regRecordMobileDTO.getFarmerName());
                    entity.setSeason(regRecordMobileDTO.getSeason());
                    entity.setMobileNumber(regRecordMobileDTO.getMobileNumber());
                    entity.setMobileStatus(regRecordMobileDTO.getMobileStatus());
                    registryTransactionsRepository.save(entity);
                }
            }
        }
        log.info("transaction table updated with combined data");
        responseHandlerService.updateCache(cacheKey);
    }

    @Override
    public AcknowledgementDTO getResponse(ResponseDTO responseDTO) throws JsonProcessingException {
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseMessageDTO messageDTO = objectMapper.convertValue(responseDTO.getMessage(), ResponseMessageDTO.class);
        String transactionId = messageDTO.getTransactionId();
        List<SearchResponseDTO> searchResponseDTO = messageDTO.getSearchResponse();
        //TODO handle this update
        //updateTransactionDbAndCache(transactionId, searchResponseDTO);

        acknowledgementDTO.setMessage(Constants.ON_SEARCH_RESPONSE_RECEIVED);
        acknowledgementDTO.setStatus(Constants.COMPLETED);
        return acknowledgementDTO;
    }
}
