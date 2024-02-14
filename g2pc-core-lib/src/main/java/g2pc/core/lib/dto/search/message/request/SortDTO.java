package g2pc.core.lib.dto.search.message.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortDTO {

    @JsonProperty("attribute_name")
    private String attributeName="YOB";

    @JsonProperty("sort_order")
    private String sortOrder="asc";
}
