package MAMS.Backend.repository;

import MAMS.Backend.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByBaseId(Long baseId);

    List<Assignment> findByEquipmentTypeId(Long equipmentTypeId);

    List<Assignment> findByPersonnelNameContainingIgnoreCase(String personnelName);
}
