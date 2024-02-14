package g2pc.core.lib.dto.search.message.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePaginationDTO {

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("page_number")
    private Integer pageNumber;

    @JsonProperty("total_count")
    private Integer totalCount;
}
