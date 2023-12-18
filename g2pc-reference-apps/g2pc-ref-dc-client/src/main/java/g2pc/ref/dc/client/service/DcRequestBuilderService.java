package g2pc.ref.dc.client.service;

import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.exceptions.G2pcError;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DcRequestBuilderService {

    //TODO: use acknowledgementDTO instead of Map
    Map<String , G2pcError> generateRequest(List<Map<String, Object>> payloadMapList) throws Exception;

    //TODO: use acknowledgementDTO instead of Map
    Map<String , G2pcError > generatePayloadFromCsv(MultipartFile payloadFile) throws Exception;
}
