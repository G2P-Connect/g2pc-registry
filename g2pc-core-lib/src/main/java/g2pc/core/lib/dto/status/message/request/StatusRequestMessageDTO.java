package g2pc.core.lib.dto.status.message.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusRequestMessageDTO {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("txnstatus_request")
    private TxnStatusRequestDTO txnStatusRequest;
}