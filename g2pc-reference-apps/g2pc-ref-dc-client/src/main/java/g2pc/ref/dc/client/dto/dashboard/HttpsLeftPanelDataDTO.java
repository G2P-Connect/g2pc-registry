package g2pc.ref.dc.client.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpsLeftPanelDataDTO {

    private String messageTs;

    private String transactionId;

    private String status;
}
