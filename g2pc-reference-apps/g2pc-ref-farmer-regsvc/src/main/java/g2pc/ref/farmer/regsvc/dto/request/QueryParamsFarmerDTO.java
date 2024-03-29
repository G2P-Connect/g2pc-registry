package g2pc.ref.farmer.regsvc.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryParamsFarmerDTO {

    @JsonProperty("farmer_id")
    private String farmerId;

    @JsonProperty("season")
    private String season;
}
