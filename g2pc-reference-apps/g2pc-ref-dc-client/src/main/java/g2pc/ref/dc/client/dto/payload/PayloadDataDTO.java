package g2pc.ref.dc.client.dto.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayloadDataDTO {

    @JsonProperty("farmer_id")
    private String farmerId;

    @JsonProperty("farmer_name")
    private String farmerName;

    @JsonProperty("mobile_number")
    private String mobileNumber;

    @JsonProperty("season")
    private String season;
}
