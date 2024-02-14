package g2pc.core.lib.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.security.service.G2pTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Date;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * The G2p token verification.
 */
@Slf4j
public class G2pTokenVerification extends OncePerRequestFilter {

    @Autowired
    G2pTokenService g2pTokenService;

    /**
     * Method to validate token
     * @param httpRequest httpRequest
     * @param httpResponse httpResponse
     * @param filterChain filterChain
     * @throws ServletException ServletException
     * @throws IOException IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain filterChain) throws ServletException, IOException
    {
        String stringToken = httpRequest.getHeader(G2pSecurityConstants.TOKEN_HEADER);

        if(null != stringToken) {
            if (stringToken.trim().equals("undefined")) {
                log.warn(" Invalid token received with undefined value for path:'{}'", httpRequest.getRequestURI());
                filterChain.doFilter(httpRequest, httpResponse);
                return;
            } try {
                stringToken = stringToken.replaceAll("Bearer ", "");
                DecodedJWT g2pDecodedJWT = JWT.decode(stringToken);

                if (g2pDecodedJWT.getExpiresAt().before(new Date())) {
                    log.warn("Token Expired:{}", httpRequest.getRequestURI());
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.getWriter().println("Token Expired");
                    return;
                } else {
                    log.info("Setting context details:{}", httpRequest.getRequestURI());
                    try {
                        Claim preferredUserNameClaim = g2pDecodedJWT.getClaim("preferred_username");
                        String path = httpRequest.getRequestURI();
                        if (preferredUserNameClaim.isMissing() || preferredUserNameClaim.isNull()) {
                            log.warn("Invalid userNameClaim in token:{}", g2pDecodedJWT.getToken());
                            throw new IllegalStateException("Invalid Token");
                        }
                        if (preferredUserNameClaim.isMissing() && path != null && path.contains("/private/")) {
                            log.warn("Invalid httpRequest made for path: {}", path);
                            throw new IllegalStateException("Unauthorized");
                        }

                    } catch (IllegalStateException e) {
                        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        httpResponse.getWriter().println("Login Required");
                        return;
                    }
                }
            } catch (Exception e) {
                log.error("Authentication Failure For Token: `{}` Path:`{}` error:{}", stringToken, httpRequest.getRequestURI(), e.getMessage(), e);
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                return;
            }
        } else {
        if (httpRequest.getRequestURI().contains("/private/")) {
            log.warn("Invalid httpRequest made for path: {}", httpRequest.getRequestURI());
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().println("Login Required");
            return;
        }
    }
        filterChain.doFilter(httpRequest, httpResponse);
    }


}