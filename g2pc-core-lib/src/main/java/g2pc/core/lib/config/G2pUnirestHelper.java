package g2pc.core.lib.config;


import g2pc.core.lib.security.context.UnirestContext;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;
import java.util.Map;

@Configuration
@Slf4j
@EnableScheduling
@Lazy
public class G2pUnirestHelper implements G2pUnirest {

    private static final String AUTH = "Authorization";


    protected GetRequest setG2pHeaders(GetRequest request) {
        return setG2pHeaders(request, Collections.emptyMap());
    }

    protected GetRequest setG2pHeaders(GetRequest request, Map<String, String> keyVal) {
        if (null != keyVal)
            keyVal.forEach((k, v) -> request.header(k, v));
        return request;
    }

    protected HttpRequestWithBody setG2pHeaders(HttpRequestWithBody request) {
        UnirestContext unirestContext = new UnirestContext();
        if (null != unirestContext && StringUtils.isNotBlank(unirestContext.getJwtHeader())) {
            request.header(AUTH, unirestContext.getJwtHeader());
        }
        return request;
    }

    public String getG2pApiCall(String uri, String token) throws UnirestException {

        return setG2pHeaders(Unirest.get(uri), Map.of(AUTH, "Bearer " + token))
                .asString()
                .getBody();

    }

    public String getG2pApiCall(String uri) throws UnirestException {

        return setG2pHeaders(Unirest.get(uri))
                .asString()
                .getBody();

    }

    public HttpRequestWithBody g2pPost(String uri) {
        return setG2pHeaders(Unirest.post(uri));
    }

    public HttpRequestWithBody g2pPut(String uri) {
        return setG2pHeaders(Unirest.put(uri));
    }

    public HttpRequestWithBody g2pDelete(String uri) {
        return setG2pHeaders(Unirest.delete(uri));
    }

    public HttpRequestWithBody g2pPatch(String uri) {
        return setG2pHeaders(Unirest.patch(uri));
    }

    public GetRequest g2pGet(String uri) {
        return setG2pHeaders(Unirest.get(uri));
    }

}
