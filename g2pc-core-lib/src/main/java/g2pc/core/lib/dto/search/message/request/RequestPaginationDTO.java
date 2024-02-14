package g2pc.core.lib.dto.search.message.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPaginationDTO {

    @JsonProperty("page_size")
    private int pageSize = 100;

    @JsonProperty("page_number")
    private int pageNumber = 1;
}
