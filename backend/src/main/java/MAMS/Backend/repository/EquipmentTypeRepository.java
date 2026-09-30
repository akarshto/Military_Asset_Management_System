package MAMS.Backend.repository;

import MAMS.Backend.entity.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentTypeRepository
        extends JpaRepository<EquipmentType, Long> {

    boolean existsByName(String name);

    Optional<EquipmentType> findByNameIgnoreCase(String name);
}