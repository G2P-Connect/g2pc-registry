package g2pc.ref.farmer.regsvc.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegRecordFarmerDTO {

    @JsonProperty("farmer_id")
    private String farmerId;

    @JsonProperty("farmer_name")
    private String farmerName;

    @JsonProperty("season")
    private String season;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("payment_date")
    private String paymentDate;

    @JsonProperty("payment_amount")
    private Double paymentAmount;
}
