package g2pc.ref.mno.regsvc.serviceimpl;

import g2pc.ref.mno.regsvc.dto.SftpDpData;
import g2pc.ref.mno.regsvc.service.DpSftpPushUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DpSftpPushUpdateServiceImpl implements DpSftpPushUpdateService {

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

    public SftpDpData getSftpDpData(String dpType, String messageTs, String transactionId, String filename){
        SftpDpData sftpDpData = new SftpDpData();
        sftpDpData.setDpType(dpType);
        sftpDpData.setMessageTs(messageTs);
        sftpDpData.setTransactionId(transactionId);
        sftpDpData.setFileName(filename);
        return sftpDpData;
    }
}
