package g2pc.core.lib.dto.status.message.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusRequestDTO {

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("header")
    private HeaderDTO header;

    @JsonProperty("message")
    private Object message;
}