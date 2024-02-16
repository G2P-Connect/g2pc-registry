package g2pc.core.lib.dto.search.message.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestMessageDTO {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("search_request")
    private List<SearchRequestDTO> searchRequest;
}
