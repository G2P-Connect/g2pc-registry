package g2pc.core.lib.dto.search.message.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequestDTO {

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("search_criteria")
    private SearchCriteriaDTO searchCriteria;

    @JsonProperty("locale")
    private String locale;
}
