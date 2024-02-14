package g2pc.core.lib.dto.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class G2pTokenResponse {

    private String accessToken;
    private String tokenType;
    private String expiresIn;
}