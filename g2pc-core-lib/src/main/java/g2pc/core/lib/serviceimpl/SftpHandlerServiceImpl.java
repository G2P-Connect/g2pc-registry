package g2pc.core.lib.serviceimpl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import g2pc.core.lib.constants.SftpConstants;
import g2pc.core.lib.dto.sftp.SftpServerConfigDTO;
import g2pc.core.lib.service.SftpHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class SftpHandlerServiceImpl implements SftpHandlerService {

    @Override
    public Boolean uploadFileToSftp(SftpServerConfigDTO serverConfigDTO, String localFilePath,
                                    String remoteDirectory) {
        ChannelSftp sftpChannel;
        try {
            sftpChannel = getJschSession(serverConfigDTO);
        } catch (IOException e) {
            return false;
        }
        try {
            sftpChannel.put(localFilePath, remoteDirectory);
            return true;
        } catch (SftpException e) {
            log.error(SftpConstants.UPLOAD_ERROR_MESSAGE, e);
            return false;
        } finally {
            sftpChannel.exit();
        }
    }

    @Override
    public ChannelSftp getJschSession(SftpServerConfigDTO serverConfigDTO) throws IOException {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel;
        try {
            session = jsch.getSession(serverConfigDTO.getUser(),
                    serverConfigDTO.getHost(),
                    serverConfigDTO.getPort());
            session.setPassword(serverConfigDTO.getPassword());
            session.setConfig(SftpConstants.STRICT_HOST_KEY_CHECKING, serverConfigDTO.getStrictHostKeyChecking());
            session.connect();
            sftpChannel = (ChannelSftp) session.openChannel(serverConfigDTO.getSessionChannel());
            sftpChannel.connect();
            return sftpChannel;
        } catch (Exception e) {
            log.error(SftpConstants.UPLOAD_ERROR_MESSAGE, e);
            if (session != null) {
                session.disconnect();
            }
            throw new IOException(e);
        }
    }
}
