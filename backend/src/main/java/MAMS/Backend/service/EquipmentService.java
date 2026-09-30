package MAMS.Backend.service;

import MAMS.Backend.dto.EquipmentRequest;
import MAMS.Backend.entity.Base;
import MAMS.Backend.entity.Equipment;
import MAMS.Backend.entity.EquipmentType;
import MAMS.Backend.repository.BaseRepository;
import MAMS.Backend.repository.EquipmentRepository;
import MAMS.Backend.repository.EquipmentTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final BaseRepository baseRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final AuditLogService auditLogService;

    public EquipmentService(
            EquipmentRepository equipmentRepository,
            BaseRepository baseRepository,
            EquipmentTypeRepository equipmentTypeRepository,
            AuditLogService auditLogService) {
        this.equipmentRepository = equipmentRepository;
        this.baseRepository = baseRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.auditLogService = auditLogService;
    }

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public Equipment createEquipment(EquipmentRequest request) {
        Base base = baseRepository.findById(request.getBaseId())
                .orElseThrow(() -> new IllegalArgumentException("Base not found"));
        String typeName = request.getEquipmentTypeName().trim();
        EquipmentType equipmentType = equipmentTypeRepository
                .findByNameIgnoreCase(typeName)
                .orElseGet(() -> equipmentTypeRepository.save(new EquipmentType(typeName)));

        Equipment equipment = new Equipment(
                request.getAssetName().trim(),
                request.getAssetCode() == null || request.getAssetCode().isBlank()
                        ? null
                        : request.getAssetCode().trim(),
                equipmentType,
                base,
                request.getQuantity());

        Equipment savedEquipment = equipmentRepository.save(equipment);
        auditLogService.logCurrentUser(
                "CREATE", "Equipment", savedEquipment.getId(),
                savedEquipment.getAssetName() + " at " + base.getName());
        return savedEquipment;
    }
}