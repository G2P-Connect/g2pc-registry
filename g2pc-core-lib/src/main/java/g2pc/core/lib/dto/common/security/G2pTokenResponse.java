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

    private String access_token;
    private String token_type;
    private String expires_in;
}