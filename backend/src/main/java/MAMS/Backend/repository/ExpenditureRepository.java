package MAMS.Backend.repository;

import MAMS.Backend.entity.Expenditure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {

    List<Expenditure> findByBaseId(Long baseId);

    List<Expenditure> findByEquipmentTypeId(Long equipmentTypeId);
}
