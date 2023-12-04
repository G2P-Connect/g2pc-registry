package g2pc.ref.dc.client.entity;

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
@Table(name = "registry_transactions")
public class RegistryTransactionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String transactionId;

    private String farmerId;

    private String farmerName;

    private String mobileNumber;

    private String season;

    private String paymentStatus;

    private String paymentDate;

    private Double paymentAmount;

    private String mobileStatus;

    @Column(insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(insertable = false)
    private Timestamp lastUpdatedDate;
}
