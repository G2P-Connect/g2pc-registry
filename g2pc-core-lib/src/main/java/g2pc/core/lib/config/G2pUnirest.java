package g2pc.core.lib.config;


import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.UnirestException;

public interface G2pUnirest {

    String getG2pApiCall(String uri, String token) throws UnirestException;

    String getG2pApiCall(String uri) throws UnirestException;

    HttpRequestWithBody g2pPost(String uri);

    HttpRequestWithBody g2pPut(String uri);

    HttpRequestWithBody g2pDelete(String uri);

    HttpRequestWithBody g2pPatch(String uri);

    GetRequest g2pGet(String uri);
}
