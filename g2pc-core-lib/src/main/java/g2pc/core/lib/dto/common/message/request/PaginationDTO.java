package g2pc.core.lib.dto.common.message.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationDTO {

    @JsonProperty("page_size")
    private int pageSize;

    @JsonProperty("page_number")
    private int pageNumber;
}
