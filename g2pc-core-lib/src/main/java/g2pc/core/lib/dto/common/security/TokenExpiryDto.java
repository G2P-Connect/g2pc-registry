package g2pc.core.lib.dto.common.security;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Data
@Getter
@Setter
public class TokenExpiryDto {

    private String token;

    private String expiresIn;

    private Timestamp dateSaved;

}
