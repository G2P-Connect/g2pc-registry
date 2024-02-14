package g2pc.core.lib.dto.status.message.response;

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
public class StatusResponseMessageDTO {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("txnstatus_response")
    private TxnStatusResponseDTO txnStatusResponse;
}
