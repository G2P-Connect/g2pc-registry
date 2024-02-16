package g2pc.ref.dc.client.service;

import g2pc.core.lib.dto.common.AcknowledgementDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DcRequestBuilderService {

    AcknowledgementDTO generateRequest(List<Map<String, Object>> payloadMapList, String protocol,
                                       String isSignEncrypt, String payloadFilename, String inboundFilename) throws Exception;

    AcknowledgementDTO generateStatusRequest(String transactionID, String transactionType, String protocol) throws Exception;

    String demoTestEncryptionSignature(File payloadFile) throws IOException;
}
