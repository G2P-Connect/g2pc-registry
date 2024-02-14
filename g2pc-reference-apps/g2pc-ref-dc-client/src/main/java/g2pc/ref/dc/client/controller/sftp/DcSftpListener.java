package g2pc.ref.dc.client.controller.sftp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.ResponseMessageDTO;
import g2pc.core.lib.dto.search.message.response.ResponseDTO;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.RequestBuilderService;
import g2pc.ref.dc.client.dto.dashboard.SftpDcData;
import g2pc.ref.dc.client.service.DcRequestBuilderService;
import g2pc.ref.dc.client.service.DcResponseHandlerService;
import g2pc.ref.dc.client.service.DcSftpPushUpdateService;
import g2pc.ref.dc.client.service.DcValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Configuration
@Slf4j
public class DcSftpListener {

    @Value("${sunbird.enabled}")
    private Boolean sunbirdEnabled;

    @Value("${sftp.listener.local.inbound_directory}")
    private String sftpLocalDirectoryInbound;

    @Value("${sftp.listener.local.outbound_directory}")
    private String sftpLocalDirectoryOutbound;

    @Autowired
    private DcRequestBuilderService dcRequestBuilderService;

    @Autowired
    private RequestBuilderService requestBuilderService;

    @Autowired
    private DcResponseHandlerService dcResponseHandlerService;

    @Autowired
    DcValidationService dcValidationService;

    @Autowired
    private DcSftpPushUpdateService dcSftpPushUpdateService;

    private Queue<SftpDcData> dataQueue = new LinkedList<>();

    /**
     * Method used to handle input listener
     *
     * @param message
     */
    @ServiceActivator(inputChannel = "sftpInbound")
    public void handleMessageInbound(Message<File> message) {
        try {
            File file = message.getPayload();
            log.info("Received Message from inbound directory: {}", file.getName());
            if (ObjectUtils.isNotEmpty(file) && file.getName().contains(".csv")) {
                AcknowledgementDTO acknowledgementDTO = dcRequestBuilderService.generateRequest(
                        requestBuilderService.generatePayloadFromCsv(file), CoreConstants.SEND_PROTOCOL_SFTP,
                        dcRequestBuilderService.demoTestEncryptionSignature(file), file.getName(), null);
                log.info("AcknowledgementDTO: {}", acknowledgementDTO);
            }
            Files.deleteIfExists(Path.of(sftpLocalDirectoryInbound + "/" + file.getName()));
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    /**
     * Method used to handle output listener
     *
     * @param message message
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @ServiceActivator(inputChannel = "sftpOutbound")
    public void handleMessageOutbound(Message<File> message) {
        try {
            File file = message.getPayload();
            log.info("Received Message from outbound directory: {}", file.getName());
            if (ObjectUtils.isNotEmpty(file) && file.getName().contains(".json")) {
                String responseString = new String(Files.readAllBytes(file.toPath()));

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerSubtypes(RequestHeaderDTO.class,
                        ResponseHeaderDTO.class, HeaderDTO.class);
                ResponseDTO responseDTO = objectMapper.readerFor(ResponseDTO.class).
                        readValue(responseString);
                ResponseMessageDTO messageDTO;
                Map<String, Object> metaData = (Map<String, Object>) responseDTO.getHeader().getMeta().getData();
                messageDTO = dcValidationService.signatureValidation(metaData, responseDTO);
                responseDTO.setMessage(messageDTO);
                try {
                    dcValidationService.validateResponseDto(responseDTO);
                    if (ObjectUtils.isNotEmpty(responseDTO)) {
                        dcResponseHandlerService.getResponse(responseDTO, file.getName(), sunbirdEnabled);
                    }
                    log.info("on search response handled successfully");
                } catch (JsonProcessingException | IllegalArgumentException e) {
                    log.info("on search response handled error : ", e);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
            Files.deleteIfExists(Path.of(sftpLocalDirectoryOutbound + "/" + file.getName()));
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    /**
     * Method to handle error
     *
     * @param message message
     */
    @ServiceActivator(inputChannel = "errorChannel")
    public void handleError(Message<?> message) {
        Throwable error = (Throwable) message.getPayload();
        log.error("Handling ERROR: {}", error.getMessage());
    }
}
