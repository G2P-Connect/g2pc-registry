package g2pc.ref.dc.client.dto.mobile.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class QueryMobileDTO{

    @JsonProperty("query_params")
    private QueryParamsMobileDTO queryParams;
}
