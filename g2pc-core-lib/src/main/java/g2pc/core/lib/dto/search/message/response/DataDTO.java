package g2pc.core.lib.dto.search.message.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataDTO {

    @JsonProperty("version")
    private String version;

    @JsonProperty("reg_type")
    private String regType;

    @JsonProperty("reg_sub_type")
    private String regSubType;

    @JsonProperty("reg_record_type")
    private String regRecordType;

    @JsonProperty("reg_records")
    private Object regRecords;
}
