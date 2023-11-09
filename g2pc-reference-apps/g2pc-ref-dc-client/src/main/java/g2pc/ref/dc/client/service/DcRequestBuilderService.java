package g2pc.ref.dc.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.message.request.MessageDTO;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.request.SearchCriteriaDTO;

import java.util.List;

public interface DcRequestBuilderService {

    List<QueryDTO> createQuery(String payloadString) throws JsonProcessingException;

    SearchCriteriaDTO getSearchCriteriaDTO(QueryDTO queryDTO, String regType);

    AcknowledgementDTO generateRequest(String payloadString) throws JsonProcessingException;
}
