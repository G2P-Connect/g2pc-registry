package g2pc.ref.farmer.regsvc.controller.sftp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.RequestDTO;
import g2pc.core.lib.dto.search.message.request.RequestMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.ref.farmer.regsvc.constants.Constants;
import g2pc.ref.farmer.regsvc.dto.SftpDpData;
import g2pc.ref.farmer.regsvc.service.DpSftpPushUpdateService;
import g2pc.ref.farmer.regsvc.service.FarmerValidationService;
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
import java.util.Map;

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
    private RequestHandlerService requestHandlerService;

    @Autowired
    FarmerValidationService farmerValidationService;

    @Autowired
    private DpSftpPushUpdateService dpSftpPushUpdateService;

    @SuppressWarnings("unchecked")
    @ServiceActivator(inputChannel = "sftpInbound")
    public void handleMessageInbound(Message<File> message) {
        try {
            File file = message.getPayload();
            log.info("Received Message from inbound directory of dp-1: {}", file.getName());
            if (ObjectUtils.isNotEmpty(file) && file.getName().contains(".json")) {
                String requestString = new String(Files.readAllBytes(file.toPath()));
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerSubtypes(RequestHeaderDTO.class,
                        ResponseHeaderDTO.class, HeaderDTO.class);

                RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
                        readValue(requestString);
                RequestMessageDTO messageDTO;

                Map<String, Object> metaData = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();

                messageDTO = farmerValidationService.signatureValidation(metaData, requestDTO);
                requestDTO.setMessage(messageDTO);
                String cacheKey = Constants.CACHE_KEY_STRING + messageDTO.getTransactionId();
                try {
                    farmerValidationService.validateRequestDTO(requestDTO);
                    requestHandlerService.buildCacheRequest(
                            objectMapper.writeValueAsString(requestDTO), cacheKey,
                            CoreConstants.SEND_PROTOCOL_SFTP, sunbirdEnabled);
                } catch (G2pcValidationException e) {
                    throw new G2pcValidationException(e.getG2PcErrorList());
                } catch (JsonProcessingException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                }
            }
            Files.deleteIfExists(Path.of(sftpLocalDirectoryInbound + "/" + file.getName()));
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    @ServiceActivator(inputChannel = "errorChannel")
    public void handleError(Message<?> message) {
        Throwable error = (Throwable) message.getPayload();
        log.error("Handling ERROR: {}", error.getMessage());
    }
}
