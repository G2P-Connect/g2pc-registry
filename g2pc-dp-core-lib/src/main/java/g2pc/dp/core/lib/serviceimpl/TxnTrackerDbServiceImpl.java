package g2pc.dp.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.RequestDTO;
import g2pc.core.lib.dto.search.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.search.message.request.ResponseMessageDTO;
import g2pc.core.lib.dto.search.message.request.SearchRequestDTO;
import g2pc.core.lib.dto.search.message.response.DataDTO;
import g2pc.core.lib.dto.search.message.response.ResponsePaginationDTO;
import g2pc.core.lib.dto.search.message.response.SearchResponseDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestMessageDTO;
import g2pc.core.lib.dto.status.message.request.TxnStatusRequestDTO;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.enums.LocalesENUM;
import g2pc.core.lib.enums.QueryTypeEnum;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dp.core.lib.constants.DpConstants;
import g2pc.dp.core.lib.entity.MsgTrackerEntity;
import g2pc.dp.core.lib.entity.TxnTrackerEntity;
import g2pc.dp.core.lib.repository.MsgTrackerRepository;
import g2pc.dp.core.lib.repository.TxnTrackerRepository;
import g2pc.dp.core.lib.service.TxnTrackerDbService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TxnTrackerDbServiceImpl implements TxnTrackerDbService {

    @Autowired
    private MsgTrackerRepository msgTrackerRepository;

    @Autowired
    private TxnTrackerRepository txnTrackerRepository;

    /**
     * Save request details
     *
     * @param requestDTO requestDTO to save in Db
     * @return request details entity
     */
    @Override
    public MsgTrackerEntity saveRequestDetails(RequestDTO requestDTO, String protocol, Boolean sunbirdEnabled) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);

        MsgTrackerEntity entity;

        HeaderDTO headerDTO = requestDTO.getHeader();
        RequestMessageDTO messageDTO = objectMapper.convertValue(requestDTO.getMessage(), RequestMessageDTO.class);
        String transactionId = messageDTO.getTransactionId();
        Optional<MsgTrackerEntity> msgTrackerEntityOptional = msgTrackerRepository.findByTransactionId(transactionId);
        if (msgTrackerEntityOptional.isEmpty()) {
            MsgTrackerEntity msgTrackerEntity = new MsgTrackerEntity();
            msgTrackerEntity.setVersion(headerDTO.getVersion());
            msgTrackerEntity.setMessageId(headerDTO.getMessageId());
            msgTrackerEntity.setMessageTs(headerDTO.getMessageTs());
            msgTrackerEntity.setAction(headerDTO.getAction());
            msgTrackerEntity.setSenderId(headerDTO.getSenderId());
            msgTrackerEntity.setReceiverId(headerDTO.getReceiverId());
            msgTrackerEntity.setIsMsgEncrypted(headerDTO.getIsMsgEncrypted());
            msgTrackerEntity.setTransactionId(messageDTO.getTransactionId());
            msgTrackerEntity.setRawMessage(objectMapper.writeValueAsString(requestDTO));
            msgTrackerEntity.setProtocol(protocol);

            List<SearchRequestDTO> searchRequestDTOList = messageDTO.getSearchRequest();
            for (SearchRequestDTO searchRequestDTO : searchRequestDTOList) {
                TxnTrackerEntity txnTrackerEntity = new TxnTrackerEntity();
                txnTrackerEntity.setReferenceId(searchRequestDTO.getReferenceId());
                txnTrackerEntity.setTimestamp(searchRequestDTO.getTimestamp());
                txnTrackerEntity.setVersion(searchRequestDTO.getSearchCriteria().getVersion());
                txnTrackerEntity.setRegType(searchRequestDTO.getSearchCriteria().getRegType());
                txnTrackerEntity.setRegSubType(searchRequestDTO.getSearchCriteria().getRegSubType());
                txnTrackerEntity.setQueryType(searchRequestDTO.getSearchCriteria().getQueryType());
                txnTrackerEntity.setQuery(objectMapper.writeValueAsString(searchRequestDTO.getSearchCriteria().getQuery()));
                txnTrackerEntity.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txnTrackerEntity.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
                txnTrackerEntity.setMsgTrackerEntity(msgTrackerEntity);
                msgTrackerEntity.getTxnTrackerEntityList().add(txnTrackerEntity);
            }
            entity = msgTrackerRepository.save(msgTrackerEntity);
        } else {
            entity = msgTrackerEntityOptional.get();
        }
        return entity;
    }

    /**
     * Get record count
     *
     * @param records required
     * @return record count
     */
    @SuppressWarnings("unchecked")
    @Override
    public int getRecordCount(Object records) {
        Map<String, Object> objectMap = new ObjectMapper().convertValue(records, Map.class);
        return objectMap.size();
    }

    /**
     * Build a search response
     *
     * @param txnTrackerEntity txnTrackerEntity to build search response
     * @param dataDTO          dataDTO to build search response dto
     * @return SearchResponseDTO
     */
    @Override
    public SearchResponseDTO buildSearchResponse(TxnTrackerEntity txnTrackerEntity, DataDTO dataDTO) {
        ResponsePaginationDTO paginationDTO = new ResponsePaginationDTO();
        paginationDTO.setPageSize(10);
        paginationDTO.setPageNumber(1);
        paginationDTO.setTotalCount(100);

        SearchResponseDTO searchResponseDTO = new SearchResponseDTO();
        searchResponseDTO.setReferenceId(txnTrackerEntity.getReferenceId());
        searchResponseDTO.setTimestamp(txnTrackerEntity.getTimestamp());
        searchResponseDTO.setStatus(txnTrackerEntity.getStatus());
        searchResponseDTO.setStatusReasonCode(txnTrackerEntity.getStatusReasonCode());
        searchResponseDTO.setStatusReasonMessage(txnTrackerEntity.getStatusReasonMessage());
        searchResponseDTO.setData(dataDTO);
        searchResponseDTO.setPagination(paginationDTO);
        searchResponseDTO.setLocale(LocalesENUM.EN.toValue());

        return searchResponseDTO;
    }

    /**
     * Build data
     *
     * @param regRecordsString regRecord to store in data dto
     * @return DataDTO
     */
    @Override
    public DataDTO buildData(String regRecordsString, TxnTrackerEntity txnTrackerEntity) {
        DataDTO dataDTO = new DataDTO();
        dataDTO.setVersion(txnTrackerEntity.getVersion());
        dataDTO.setRegType(txnTrackerEntity.getRegType());
        dataDTO.setRegSubType(txnTrackerEntity.getRegSubType());
        dataDTO.setRegRecordType(txnTrackerEntity.getRegRecordType());
        if (StringUtils.isNotEmpty(regRecordsString)) {
            dataDTO.setRegRecords(regRecordsString);
        } else {
            dataDTO.setRegRecords(null);
        }
        return dataDTO;
    }

    /**
     * Get updated search response list
     *
     * @param requestDTO            request dto to be updated
     * @param refRecordsStringsList list of records
     * @return updated search response list
     */
    @Override
    public List<SearchResponseDTO> getUpdatedSearchResponseList(RequestDTO requestDTO,
                                                                List<String> refRecordsStringsList,
                                                                String protocol,
                                                                Boolean sunbirdEnabled) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);
        List<SearchResponseDTO> searchResponseDTOList = new ArrayList<>();
        MsgTrackerEntity msgTrackerEntity = saveRequestDetails(requestDTO, protocol, sunbirdEnabled);

        List<TxnTrackerEntity> txnTrackerEntityList = msgTrackerEntity.getTxnTrackerEntityList();
        int totalCount = txnTrackerEntityList.size();
        int completedCount = 0;
        int index = 0;
        for (TxnTrackerEntity txnTrackerEntity : txnTrackerEntityList) {
            String refRecordsString = refRecordsStringsList.get(index);

            txnTrackerEntity.setConsent(true);
            txnTrackerEntity.setAuthorize(true);
            txnTrackerEntity.setRegRecordType(QueryTypeEnum.NAMEDQUERY.toValue());
            DataDTO dataDTO = buildData(refRecordsString, txnTrackerEntity);
            if (refRecordsString.isEmpty()) {
                txnTrackerEntity.setStatus(HeaderStatusENUM.RJCT.toValue());
                txnTrackerEntity.setStatusReasonCode(DpConstants.RECORD_NOT_FOUND);
                txnTrackerEntity.setStatusReasonMessage(DpConstants.RECORD_NOT_FOUND);
                txnTrackerEntity.setNoOfRecords(0);
            } else {
                txnTrackerEntity.setStatus(HeaderStatusENUM.SUCC.toValue());
                txnTrackerEntity.setStatusReasonCode(HeaderStatusENUM.SUCC.toValue());
                txnTrackerEntity.setStatusReasonMessage(HeaderStatusENUM.SUCC.toValue());
                txnTrackerEntity.setNoOfRecords(1);
                completedCount++;
            }
            searchResponseDTOList.add(buildSearchResponse(txnTrackerEntity, dataDTO));
            index++;
        }
        msgTrackerEntity.setTxnTrackerEntityList(txnTrackerEntityList);
        msgTrackerEntity.setStatus(HeaderStatusENUM.SUCC.toValue());
        msgTrackerEntity.setStatusReasonCode(HeaderStatusENUM.SUCC.toValue());
        msgTrackerEntity.setStatusReasonMessage(HeaderStatusENUM.SUCC.toValue());
        msgTrackerEntity.setTotalCount(totalCount);
        msgTrackerEntity.setCompletedCount(completedCount);
        msgTrackerEntity.setCorrelationId(CommonUtils.generateUniqueId("C"));
        msgTrackerEntity.setLocale(LocalesENUM.EN.toValue());
        msgTrackerRepository.save(msgTrackerEntity);
        return searchResponseDTOList;
    }

    /**
     * @param statusRequestDTO statusRequestDTO to save in db
     * @return MsgTrackerEntity
     * @throws JsonProcessingException jsonProcessingException might be thrown
     */
    @Override
    public MsgTrackerEntity saveStatusRequestDetails(StatusRequestDTO statusRequestDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);

        MsgTrackerEntity entity;

        HeaderDTO headerDTO = statusRequestDTO.getHeader();
        StatusRequestMessageDTO messageDTO = objectMapper.convertValue(statusRequestDTO.getMessage(), StatusRequestMessageDTO.class);
        String transactionId = messageDTO.getTransactionId();
        Optional<MsgTrackerEntity> msgTrackerEntityOptional = msgTrackerRepository.findByTransactionId(transactionId);
        if (msgTrackerEntityOptional.isEmpty()) {
            MsgTrackerEntity msgTrackerEntity = new MsgTrackerEntity();
            msgTrackerEntity.setVersion(headerDTO.getVersion());
            msgTrackerEntity.setMessageId(headerDTO.getMessageId());
            msgTrackerEntity.setMessageTs(headerDTO.getMessageTs());
            msgTrackerEntity.setAction(headerDTO.getAction());
            msgTrackerEntity.setSenderId(headerDTO.getSenderId());
            msgTrackerEntity.setReceiverId(headerDTO.getReceiverId());
            msgTrackerEntity.setIsMsgEncrypted(headerDTO.getIsMsgEncrypted());
            msgTrackerEntity.setTransactionId(messageDTO.getTransactionId());
            msgTrackerEntity.setRawMessage(objectMapper.writeValueAsString(statusRequestDTO));

            TxnStatusRequestDTO txnStatusRequestDTO = messageDTO.getTxnStatusRequest();
            TxnTrackerEntity txnTrackerEntity = new TxnTrackerEntity();
            txnTrackerEntity.setTimestamp(null);
            txnTrackerEntity.setVersion(null);
            txnTrackerEntity.setRegType(null);
            txnTrackerEntity.setRegSubType(null);
            txnTrackerEntity.setQueryType(null);
            txnTrackerEntity.setQuery(null);
            txnTrackerEntity.setTxnStatus(null);
            txnTrackerEntity.setTxnType(txnStatusRequestDTO.getTxnType());
            txnTrackerEntity.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            txnTrackerEntity.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
            txnTrackerEntity.setMsgTrackerEntity(msgTrackerEntity);
            msgTrackerEntity.getTxnTrackerEntityList().add(txnTrackerEntity);

            entity = msgTrackerRepository.save(msgTrackerEntity);
        } else {
            entity = msgTrackerEntityOptional.get();
        }
        return entity;
    }

    /**
     * @param responseMessageDTO responseMessageDTO to update in db
     * @param transactionId      transactionId to search in db
     */
    @Override
    public void updateStatusResponseDetails(ResponseMessageDTO responseMessageDTO, String transactionId) {

        Optional<MsgTrackerEntity> msgTrackerEntityOptional = msgTrackerRepository.findByTransactionId(transactionId);
        MsgTrackerEntity msgTrackerEntity = msgTrackerEntityOptional.get();
        Optional<TxnTrackerEntity> trackerEntityOptional = txnTrackerRepository.findByMsgTrackerEntity(msgTrackerEntity);
        TxnTrackerEntity txnTrackerEntity = trackerEntityOptional.get();
        txnTrackerEntity.setTxnStatus(responseMessageDTO.toString());
        txnTrackerRepository.save(txnTrackerEntity);
    }

    /**
     * @param transactionId transactionId used to search data
     */
    @Override
    public void updateMessageTrackerStatusDb(String transactionId) {
        Optional<MsgTrackerEntity> msgTrackerEntityOptional = msgTrackerRepository.findByTransactionId(transactionId);
        MsgTrackerEntity msgTrackerEntity = msgTrackerEntityOptional.get();
        msgTrackerEntity.setStatus(HeaderStatusENUM.SUCC.toValue());
        msgTrackerEntity.setStatusReasonCode(HeaderStatusENUM.SUCC.toValue());
        msgTrackerEntity.setStatusReasonMessage(HeaderStatusENUM.SUCC.toValue());
        msgTrackerEntity.setCorrelationId(CommonUtils.generateUniqueId("C"));
        msgTrackerRepository.save(msgTrackerEntity);
    }

}
