package g2pc.ref.mno.regsvc.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DataMobileDTO{

    @JsonProperty("reg_records")
    private List<RegRecordMobileDTO> regRecords;
}
