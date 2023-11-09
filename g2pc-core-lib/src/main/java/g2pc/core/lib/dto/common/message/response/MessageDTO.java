package g2pc.core.lib.dto.common.message.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDTO {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("search_response")
    private SearchResponseDTO searchResponse;
}
