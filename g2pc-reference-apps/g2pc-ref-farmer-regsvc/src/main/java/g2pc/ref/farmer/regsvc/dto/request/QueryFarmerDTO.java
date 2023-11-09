package g2pc.ref.farmer.regsvc.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryFarmerDTO {

    @JsonProperty("query_params")
    private QueryParamsFarmerDTO queryParams;
}
