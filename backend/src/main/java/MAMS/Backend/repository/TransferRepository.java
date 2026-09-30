package MAMS.Backend.repository;

import MAMS.Backend.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByFromBaseId(Long baseId);

    List<Transfer> findByToBaseId(Long baseId);

    List<Transfer> findByEquipmentTypeId(Long equipmentTypeId);
}
