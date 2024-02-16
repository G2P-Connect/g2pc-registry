package g2pc.core.lib.dto.search.message.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryDTO {

    @JsonProperty("query_name")
    private String queryName;

    @JsonProperty("query_params")
    private Object queryParams;
}
