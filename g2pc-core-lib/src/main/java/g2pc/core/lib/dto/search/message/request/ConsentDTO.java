package g2pc.core.lib.dto.search.message.request;

import g2pc.core.lib.dto.common.PurposeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentDTO {

    private String ts;

    private PurposeDTO purpose;
}
