package g2pc.core.lib.dto.common.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeName("responseHeader")
public class ResponseHeaderDTO extends HeaderDTO {

    private String status;

    @JsonProperty("status_reason_code")
    private String statusReasonCode;

    @JsonProperty("status_reason_message")
    private String statusReasonMessage;

    @JsonProperty("completed_count")
    private Integer completedCount;

    public ResponseHeaderDTO(String messageId,
                             String messageTs,
                             String action,
                             Integer totalCount,
                             String senderId,
                             String receiverId,
                             Boolean isMsgEncrypted,
                             MetaDTO meta,
                             String status,
                             String statusReasonCode,
                             String statusReasonMessage,
                             Integer completedCount) {

        super(messageId, messageTs, action, totalCount, senderId, receiverId, isMsgEncrypted, meta);
        this.status = status;
        this.statusReasonCode=statusReasonCode;
        this.statusReasonMessage=statusReasonMessage;
        this.completedCount=completedCount;
    }
}
