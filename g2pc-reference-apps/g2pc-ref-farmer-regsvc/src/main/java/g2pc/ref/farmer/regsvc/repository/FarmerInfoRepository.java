package g2pc.ref.farmer.regsvc.repository;

import g2pc.ref.farmer.regsvc.entity.FarmerInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerInfoRepository extends JpaRepository<FarmerInfoEntity, Long> {
    Optional<FarmerInfoEntity> findBySeasonAndFarmerId(String season, String farmerId);
}
