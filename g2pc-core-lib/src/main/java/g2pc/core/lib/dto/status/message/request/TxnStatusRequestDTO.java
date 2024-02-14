package g2pc.core.lib.dto.status.message.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TxnStatusRequestDTO {

    @JsonProperty("txn_type")
    private String txnType;

    @JsonProperty("attribute_type")
    private String attributeType;

    @JsonProperty("attribute_value")
    private Object attributeValue;

    @JsonProperty("locale")
    private String locale;
}
