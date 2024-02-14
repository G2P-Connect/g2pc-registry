package g2pc.ref.mno.regsvc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SftpDpData {

    private String dpType;

    private String messageTs;

    private String transactionId;

    private String fileName;
}
