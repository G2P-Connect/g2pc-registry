package g2pc.ref.dc.client.serviceimpl;

import g2pc.core.lib.utils.CommonUtils;
import g2pc.ref.dc.client.dto.dashboard.SftpDcData;
import g2pc.ref.dc.client.service.DcSftpPushUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DcSftpPushUpdateServiceImpl implements DcSftpPushUpdateService {

    private final List<SseEmitter> emitters = new ArrayList<>();

    public SseEmitter register() {
        int minutes = 15;
        long timeout = (long) minutes * 60000;
        SseEmitter emitter = new SseEmitter(timeout);
        this.emitters.add(emitter);
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        log.info("SSE emitter registered" + emitter);
        return emitter;
    }

    public void pushUpdate(Object update) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        this.emitters.forEach(emitter -> {
            try {
                emitter.send(update);
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        this.emitters.removeAll(deadEmitters);
    }

    public SftpDcData buildSftpDcData(String transactionId, String filename) {
        SftpDcData sftpDcData = new SftpDcData();
        sftpDcData.setMessageTs(CommonUtils.getCurrentTimeStamp());
        sftpDcData.setTransactionId(transactionId);
        sftpDcData.setFileName(filename);
        sftpDcData.setSftpDirectoryType("INBOUND");
        return sftpDcData;
    }
}
