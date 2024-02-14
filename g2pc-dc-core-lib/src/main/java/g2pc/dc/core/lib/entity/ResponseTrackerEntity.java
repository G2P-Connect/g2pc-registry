package g2pc.dc.core.lib.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "response_tracker", schema = "g2pc")
public class ResponseTrackerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version")
    private String version;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "message_ts")
    private String messageTs;

    @Column(name = "action")
    private String action;

    @Column(name = "status")
    private String status;

    @Column(name = "status_reason_code")
    private String statusReasonCode;

    @Column(name = "status_reason_message")
    private String statusReasonMessage;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "completed_count")
    private Integer completedCount;

    @Column(name = "sender_id")
    private String senderId;

    @Column(name = "receiver_id")
    private String receiverId;

    @Column(name = "is_msg_encrypted")
    private Boolean isMsgEncrypted;

    @Column(name = "meta")
    private String meta;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "registry_type")
    private String registryType;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "payload_filename")
    private String payloadFilename;

    @Column(name = "inbound_filename")
    private String inboundFilename;

    @Column(name = "outbound_filename")
    private String outboundFilename;

    @Column(insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(insertable = false)
    private Timestamp lastUpdatedDate;

    @ToString.Exclude
    @OneToMany(mappedBy = "responseTrackerEntity", cascade = CascadeType.ALL)
    private List<ResponseDataEntity> responseDataEntityList = new ArrayList<>();
}
