package g2pc.core.lib.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AcknowledgementDTO {

    @JsonProperty("message")
    private Object message;

    @JsonProperty("status")
    private String status;
}
