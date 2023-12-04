package g2pc.core.lib.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurposeDTO {

    private String text;

    private String code;

    private String refUri;
}
