package g2pc.core.lib.dto.common.message.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDTO {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("search_request")
    private SearchRequestDTO searchRequest;
}
