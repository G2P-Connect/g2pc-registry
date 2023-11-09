package g2pc.ref.dc.client.dto.mobile.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegRecordMobileDTO {

    @JsonProperty("farmer_id")
    private String farmerId;

    @JsonProperty("farmer_name")
    private String farmerName;

    @JsonProperty("season")
    private String season;

    @JsonProperty("mobile_number")
    private String mobileNumber;

    @JsonProperty("mobile_status")
    private String mobileStatus;

    @JsonProperty("created_date")
    private String createdDate;
}
