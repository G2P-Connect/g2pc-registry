package g2pc.dc.core.lib.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "response_data", schema = "g2pc")
public class ResponseDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_id")
    private String referenceId;

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

    @Column(name = "reg_record_type")
    private String regRecordType;

    @Column(name = "reg_records")
    private String regRecords;

    @Column(insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(insertable = false)
    private Timestamp lastUpdatedDate;

    @ManyToOne(targetEntity = ResponseTrackerEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "registry_transactions_id", referencedColumnName = "id")
    private ResponseTrackerEntity responseTrackerEntity;
}
