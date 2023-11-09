package g2pc.ref.dc.client.dto.mobile.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class QueryParamsMobileDTO {

    @JsonProperty("mobile_number")
    private List<String> mobileNumber;

    @JsonProperty("season")
    private String season;
}
