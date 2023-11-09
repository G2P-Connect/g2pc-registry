package g2pc.ref.farmer.regsvc.dto.response;

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
public class DataFarmerDTO  {

    @JsonProperty("reg_records")
    private List<RegRecordFarmerDTO> regRecords;
}
