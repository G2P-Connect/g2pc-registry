package g2pc.dp.core.lib.repository;

import g2pc.dp.core.lib.entity.MsgTrackerEntity;
import g2pc.dp.core.lib.entity.TxnTrackerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TxnTrackerRepository extends JpaRepository<TxnTrackerEntity, Long> {

    Optional<TxnTrackerEntity> findByMsgTrackerEntity(MsgTrackerEntity msgTrackerEntity);
}
