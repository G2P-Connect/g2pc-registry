package g2pc.ref.mno.regsvc.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryParamsMobileDTO {

    @JsonProperty("mobile_number")
    private List<String> mobileNumber;

    @JsonProperty("season")
    private String season;
}
