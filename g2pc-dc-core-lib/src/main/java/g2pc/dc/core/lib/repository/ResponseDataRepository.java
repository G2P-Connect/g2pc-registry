package g2pc.dc.core.lib.repository;

import g2pc.dc.core.lib.entity.ResponseDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResponseDataRepository extends JpaRepository<ResponseDataEntity, Long> {

    Optional<ResponseDataEntity> findByReferenceId(String referenceId);
}
