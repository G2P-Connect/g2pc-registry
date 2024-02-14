package g2pc.core.lib.dto.status.message.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TxnStatusResponseDTO {


    @JsonProperty("txn_type")
    private String txnType;

    @JsonProperty("txn_status")
    private Object txnStatus;

}
