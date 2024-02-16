package g2pc.core.lib.dto.common.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeName("requestHeader")
public class RequestHeaderDTO extends HeaderDTO {

    @JsonProperty("sender_uri")
    private String senderUri;

    public RequestHeaderDTO(String messageId,
                            String messageTs,
                            String action,
                            Integer totalCount,
                            String senderId,
                            String receiverId,
                            Boolean isMsgEncrypted,
                            MetaDTO meta,
                            String senderUri) {

        super(messageId, messageTs, action, totalCount, senderId, receiverId, isMsgEncrypted, meta);
        this.senderUri = senderUri;
    }
}
