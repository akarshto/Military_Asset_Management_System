package MAMS.Backend.repository;

import MAMS.Backend.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByBaseId(Long baseId);

    List<Purchase> findByEquipmentTypeId(Long equipmentTypeId);
}
