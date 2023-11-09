package g2pc.ref.dc.client.dto.farmer.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryFarmerDTO {

    @JsonProperty("query_params")
    private QueryParamsFarmerDTO queryParams;
}
