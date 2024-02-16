package g2pc.core.lib.dto.common.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheDTO {

    @JsonProperty("data")
    private String data;

    @JsonProperty("status")
    private String status;

    @JsonProperty("protocol")
    private String protocol;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("last_updated_date")
    private String lastUpdatedDate;
}
