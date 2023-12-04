package g2pc.dp.core.lib.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "txn_tracker")
public class TxnTrackerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "consent")
    private Boolean consent;

    @Column(name = "authorize")
    private Boolean authorize;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "status")
    private String status;

    @Column(name = "status_reason_code")
    private String statusReasonCode;

    @Column(name = "status_reason_message")
    private String statusReasonMessage;

    @Column(name = "version")
    private String version;

    @Column(name = "reg_type")
    private String regType;

    @Column(name = "reg_sub_type")
    private String regSubType;

    @Column(name = "query_type")
    private String queryType;

    @Column(name = "query")
    private String query;

    @Column(name = "reg_record_type")
    private String regRecordType;

    @Column(name = "no_of_records")
    private Integer noOfRecords;

    @Column(insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(insertable = false)
    private Timestamp lastUpdatedDate;

    @ManyToOne(targetEntity = MsgTrackerEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "msg_tracker_id", referencedColumnName = "id")
    private MsgTrackerEntity msgTrackerEntity;
}