package g2pc.ref.farmer.regsvc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "farmer_info")
public class FarmerInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String farmerId;

    private String farmerName;

    private String season;

    private String paymentStatus;

    private String paymentDate;

    private Double paymentAmount;
}
