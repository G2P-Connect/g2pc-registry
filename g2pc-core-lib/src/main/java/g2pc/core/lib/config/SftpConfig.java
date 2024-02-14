package g2pc.core.lib.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.messaging.MessageChannel;

import java.io.File;

@Configuration
@Slf4j
public class SftpConfig {

    @Value("${sftp.listener.local.inbound_directory}")
    private String sftpLocalDirectoryInbound;

    @Value("${sftp.listener.local.outbound_directory}")
    private String sftpLocalDirectoryOutbound;

    @Bean
    public MessageChannel sftpInbound() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel sftpOutbound() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel errorChannel() {
        return new DirectChannel();
    }

    @Bean
    @InboundChannelAdapter(channel = "sftpInbound", poller = @Poller(fixedDelay = "5000"))
    public MessageSource<File> fileReadingMessageSourceInbound() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File(sftpLocalDirectoryInbound));
        source.setAutoCreateDirectory(true);
        source.setFilter(new AcceptOnceFileListFilter<>());
        return source;
    }

    @Bean
    @InboundChannelAdapter(channel = "sftpOutbound", poller = @Poller(fixedDelay = "5000"))
    public MessageSource<File> fileReadingMessageSourceOutbound() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File(sftpLocalDirectoryOutbound));
        source.setAutoCreateDirectory(true);
        source.setFilter(new AcceptOnceFileListFilter<>());
        return source;
    }
}