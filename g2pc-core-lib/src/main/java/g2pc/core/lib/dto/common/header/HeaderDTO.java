package g2pc.core.lib.dto.common.header;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
})
public abstract class HeaderDTO {

    private String version = "1.0.0";

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("message_ts")
    private String messageTs;

    private String action;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("sender_id")
    private String senderId;

    @JsonProperty("receiver_id")
    private String receiverId;

    @JsonProperty("is_msg_encrypted")
    private Boolean isMsgEncrypted;

    private MetaDTO meta;

    public HeaderDTO() {
    }

    public HeaderDTO(String messageId,
                     String messageTs,
                     String action,
                     Integer totalCount,
                     String senderId,
                     String receiverId,
                     Boolean isMsgEncrypted,
                     MetaDTO meta) {

    }
}