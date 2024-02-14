package g2pc.dc.core.lib.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import g2pc.core.lib.utils.CommonUtils;
import lombok.*;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class ResponseTrackerDto {
    public ResponseTrackerDto() {
        this.version = "";
        this.messageId = "";
        this.messageTs =  "";
        this.action =  "";
        this.status =  "";
        this.statusReasonCode =  "";
        this.statusReasonMessage =  "";
        this.totalCount =  0;
        this.completedCount =  0;
        this.senderId =  "";
        this.receiverId =  "";
        this.isMsgEncrypted =  false;
        this.meta =  "";
        this.transactionId =  "";
        this.correlationId =  "";
        this.registryType =  "";
        this.protocol =  "";
        this.payloadFilename =  "";
        this.inboundFilename =  "";
        this.outboundFilename =  "";
        this.createdDate =  CommonUtils.getCurrentTimeStamp();
        this.lastUpdatedDate =  CommonUtils.getCurrentTimeStamp();
    }

    @JsonProperty(  "version")
    private String version;

    @ JsonProperty(  "message_id")
    private String messageId;

    @ JsonProperty(  "message_ts")
    private String messageTs;

    @ JsonProperty(  "action")
    private String action;

    @ JsonProperty(  "status")
    private String status;

    @ JsonProperty(  "status_reason_code")
    private String statusReasonCode;

    @ JsonProperty(  "status_reason_message")
    private String statusReasonMessage;

    @ JsonProperty(  "total_count")
    private Integer totalCount;

    @ JsonProperty(  "completed_count")
    private Integer completedCount;

    @ JsonProperty(  "sender_id")
    private String senderId;

    @ JsonProperty(  "receiver_id")
    private String receiverId;

    @ JsonProperty(  "is_msg_encrypted")
    private Boolean isMsgEncrypted;

    @ JsonProperty(  "meta")
    private String meta;

    @ JsonProperty(  "transaction_id")
    private String transactionId;

    @ JsonProperty(  "correlation_id")
    private String correlationId;

    @ JsonProperty(  "registry_type")
    private String registryType;

    @ JsonProperty(  "protocol")
    private String protocol;

    @ JsonProperty(  "payload_filename")
    private String payloadFilename;

    @ JsonProperty(  "inbound_filename")
    private String inboundFilename;

    @ JsonProperty(  "outbound_filename")
    private String outboundFilename;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("last_updated_date")
    private String lastUpdatedDate;
}
