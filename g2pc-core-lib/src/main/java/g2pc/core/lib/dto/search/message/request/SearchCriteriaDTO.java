package g2pc.core.lib.dto.search.message.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteriaDTO {

    @JsonProperty("version")
    private String version;

    @JsonProperty("reg_type")
    private String regType;

    @JsonProperty("reg_sub_type")
    private String regSubType;

    @JsonProperty("query_type")
    private String queryType;

    @JsonProperty("query")
    private QueryDTO query;

    @JsonProperty("sort")
    private List<SortDTO> sort;

    @JsonProperty("pagination")
    private RequestPaginationDTO pagination;

    @JsonProperty("consent")
    private ConsentDTO consent;

    @JsonProperty("authorize")
    private AuthorizeDTO authorize;
}