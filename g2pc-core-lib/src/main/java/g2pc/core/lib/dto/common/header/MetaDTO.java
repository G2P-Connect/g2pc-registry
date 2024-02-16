package g2pc.core.lib.dto.common.header;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetaDTO {

    private Object data;
}
