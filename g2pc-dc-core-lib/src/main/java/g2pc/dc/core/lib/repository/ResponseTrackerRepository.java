package g2pc.dc.core.lib.repository;

import g2pc.dc.core.lib.entity.ResponseTrackerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResponseTrackerRepository extends JpaRepository<ResponseTrackerEntity, Long> {

    Optional<ResponseTrackerEntity> findByTransactionId(String transactionId);

    Optional<List<ResponseTrackerEntity>> findAllByAction(String action);
}
