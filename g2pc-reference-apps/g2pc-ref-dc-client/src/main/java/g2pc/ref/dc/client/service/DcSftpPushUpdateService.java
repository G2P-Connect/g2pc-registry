package g2pc.ref.dc.client.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface DcSftpPushUpdateService {

    SseEmitter register();

    void pushUpdate(Object update);
}
