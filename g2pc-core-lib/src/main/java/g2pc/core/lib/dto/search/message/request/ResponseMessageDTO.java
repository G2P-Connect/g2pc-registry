package g2pc.core.lib.dto.search.message.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import g2pc.core.lib.dto.search.message.response.SearchResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseMessageDTO {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("search_response")
    private List<SearchResponseDTO> searchResponse;
}
