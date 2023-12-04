package g2pc.ref.dc.client.service;

import g2pc.core.lib.dto.common.AcknowledgementDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DcRequestBuilderService {

    void createInitialTransactionInDB(String transactionId);

    AcknowledgementDTO generateRequest(List<Map<String, Object>> payloadMapList) throws Exception;

    AcknowledgementDTO generatePayloadFromCsv(MultipartFile payloadFile) throws Exception;
}
