package g2pc.core.lib.dto.search.message.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseDTO {

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("status")
    private String status;

    @JsonProperty("status_reason_code")
    private String statusReasonCode;

    @JsonProperty("status_reason_message")
    private String statusReasonMessage;

    @JsonProperty("data")
    private DataDTO data;

    @JsonProperty("pagination")
    private ResponsePaginationDTO pagination;

    @JsonProperty("locale")
    private String locale;
}
