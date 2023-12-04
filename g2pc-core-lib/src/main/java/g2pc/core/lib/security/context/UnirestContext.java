package g2pc.core.lib.security.context;


import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(value="request", proxyMode= ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class UnirestContext {

    List<String> roles;

    String jwtHeader;
}
