package g2pc.dp.core.lib.repository;

import g2pc.dp.core.lib.entity.MsgTrackerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MsgTrackerRepository extends JpaRepository<MsgTrackerEntity, Long> {

    Optional<MsgTrackerEntity> findByTransactionId(String transactionId);
}
