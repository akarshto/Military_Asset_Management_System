package MAMS.Backend.repository;

import MAMS.Backend.entity.Base;
import MAMS.Backend.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByBase(Base base);

    List<Equipment> findByBaseId(Long baseId);

    Optional<Equipment> findFirstByBaseIdAndEquipmentTypeId(Long baseId, Long equipmentTypeId);
}