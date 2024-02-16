package g2pc.core.lib.dto.sftp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SftpServerConfigDTO {

    private String host;

    private int port;

    private String user;

    private String password;

    private Boolean allowUnknownKeys;

    private String strictHostKeyChecking;

    private String sessionChannel = "sftp";

    private String remoteInboundDirectory;

    private String remoteOutboundDirectory;

    private String localInboundDirectory;

    private String localOutboundDirectory;
}

