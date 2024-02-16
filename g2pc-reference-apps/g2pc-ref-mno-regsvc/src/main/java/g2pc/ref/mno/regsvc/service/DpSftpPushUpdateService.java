package g2pc.ref.mno.regsvc.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface DpSftpPushUpdateService {

    SseEmitter register();

    void pushUpdate(Object update);
}
