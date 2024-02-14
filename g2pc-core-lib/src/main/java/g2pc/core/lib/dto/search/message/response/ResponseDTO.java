package g2pc.core.lib.dto.search.message.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("header")
    private HeaderDTO header;

    @JsonProperty("message")
    private Object message;
}
