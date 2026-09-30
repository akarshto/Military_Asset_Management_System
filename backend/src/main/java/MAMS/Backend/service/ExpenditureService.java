package MAMS.Backend.service;

import MAMS.Backend.entity.Expenditure;
import MAMS.Backend.entity.Base;
import MAMS.Backend.entity.Equipment;
import MAMS.Backend.entity.EquipmentType;
import MAMS.Backend.repository.BaseRepository;
import MAMS.Backend.repository.EquipmentRepository;
import MAMS.Backend.repository.EquipmentTypeRepository;
import MAMS.Backend.repository.ExpenditureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final BaseRepository baseRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final AuditLogService auditLogService;

    public ExpenditureService(
            ExpenditureRepository expenditureRepository,
            BaseRepository baseRepository,
            EquipmentRepository equipmentRepository,
            EquipmentTypeRepository equipmentTypeRepository,
            AuditLogService auditLogService) {
        this.expenditureRepository = expenditureRepository;
        this.baseRepository = baseRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.auditLogService = auditLogService;
    }

    public List<Expenditure> getAll() {
        return expenditureRepository.findAll();
    }

    public Expenditure getById(Long id) {
        return expenditureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expenditure not found"));
    }

    @Transactional
    public Expenditure create(Expenditure expenditure) {
        if (expenditure.getQuantity() == null || expenditure.getQuantity() <= 0) {
            throw new IllegalArgumentException("Expenditure quantity must be positive");
        }
        Base base = baseRepository.findById(expenditure.getBase().getId())
                .orElseThrow(() -> new IllegalArgumentException("Base not found"));
        EquipmentType equipmentType = equipmentTypeRepository
                .findById(expenditure.getEquipmentType().getId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment type not found"));
        Equipment equipment = equipmentRepository
                .findFirstByBaseIdAndEquipmentTypeId(base.getId(), equipmentType.getId())
                .orElseThrow(() -> new IllegalArgumentException("No matching equipment at this base"));
        if (equipment.getQuantity() < expenditure.getQuantity()) {
            throw new IllegalArgumentException("Expenditure exceeds available equipment");
        }
        equipment.setQuantity(equipment.getQuantity() - expenditure.getQuantity());
        equipmentRepository.save(equipment);
        expenditure.setBase(base);
        expenditure.setEquipmentType(equipmentType);
        Expenditure savedExpenditure = expenditureRepository.save(expenditure);
        auditLogService.logCurrentUser(
                "CREATE", "Expenditure", savedExpenditure.getId(),
                expenditure.getQuantity() + " " + equipmentType.getName() + " at " + base.getName());
        return savedExpenditure;
    }

    @Transactional
    public void delete(Long id) {
        Expenditure expenditure = getById(id);
        Equipment equipment = equipmentRepository
                .findFirstByBaseIdAndEquipmentTypeId(
                        expenditure.getBase().getId(), expenditure.getEquipmentType().getId())
                .orElseGet(() -> new Equipment(
                        expenditure.getEquipmentType().getName(), null, expenditure.getEquipmentType(),
                        expenditure.getBase(), 0));
        equipment.setQuantity(equipment.getQuantity() + expenditure.getQuantity());
        equipmentRepository.save(equipment);
        expenditureRepository.delete(expenditure);
        auditLogService.logCurrentUser("DELETE", "Expenditure", id, "Expenditure " + id + " reversed");
    }

    public List<Expenditure> getByBase(Long baseId) {
        return expenditureRepository.findByBaseId(baseId);
    }

    public List<Expenditure> getByEquipmentType(Long equipmentTypeId) {
        return expenditureRepository.findByEquipmentTypeId(equipmentTypeId);
    }
}