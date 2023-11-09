package g2pc.ref.dc.client.repository;

import g2pc.ref.dc.client.entity.RegistryTransactionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistryTransactionsRepository extends JpaRepository<RegistryTransactionsEntity, Long> {

    Optional<RegistryTransactionsEntity> getByTransactionId(String transactionId);

    Optional<RegistryTransactionsEntity> getByTransactionIdAndFarmerId(String transactionId,String farmerId);

    Optional<RegistryTransactionsEntity> getByTransactionIdAndMobileNumber(String transactionId,String mobileNumber);
}
