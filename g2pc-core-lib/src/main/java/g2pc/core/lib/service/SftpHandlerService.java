package g2pc.core.lib.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import g2pc.core.lib.dto.sftp.SftpServerConfigDTO;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.io.IOException;

public interface SftpHandlerService {

    Boolean uploadFileToSftp(SftpServerConfigDTO serverConfigDTO, String localFilePath, String remoteDirectory);

    ChannelSftp getJschSession(SftpServerConfigDTO serverConfigDTO) throws IOException;
}
