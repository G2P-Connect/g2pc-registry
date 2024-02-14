package g2pc.dc.core.lib.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import g2pc.core.lib.utils.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDataDto {

    public ResponseDataDto() {
        this.registryTransactionsId = "";
        this.referenceId = "";
        this.timestamp = "";
        this.status = "";
        this.statusReasonCode = "";
        this.statusReasonMessage = "";
        this.version = "";
        this.regType = "";
        this.regSubType = "";
        this.regRecordType = "";
        this.regRecords = "";
        this.txnType = "";
        this.attributeType = "";
        this.attributeValue = "";
        this.createdDate = CommonUtils.getCurrentTimeStamp();
        this.lastUpdatedDate = CommonUtils.getCurrentTimeStamp();
    }

    @JsonProperty("registry_transactions_id")
    private String registryTransactionsId;

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("status")
    private String status;

    @JsonProperty("status_reason_code")
    private String statusReasonCode;

    @JsonProperty("status_reason_message")
    private String statusReasonMessage;

    @JsonProperty("version")
    private String version;

    @JsonProperty("reg_type")
    private String regType;

    @JsonProperty("reg_sub_type")
    private String regSubType;

    @JsonProperty("reg_record_type")
    private String regRecordType;

    @JsonProperty("reg_records")
    private String regRecords;

    @JsonProperty("txn_type")
    private String txnType;

    @JsonProperty("attribute_type")
    private String attributeType;

    @JsonProperty("attribute_value")
    private String attributeValue;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("last_updated_date")
    private String lastUpdatedDate;
}