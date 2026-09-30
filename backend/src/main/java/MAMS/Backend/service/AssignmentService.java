package MAMS.Backend.service;

import MAMS.Backend.entity.Assignment;
import MAMS.Backend.entity.Base;
import MAMS.Backend.entity.Equipment;
import MAMS.Backend.entity.EquipmentType;
import MAMS.Backend.repository.AssignmentRepository;
import MAMS.Backend.repository.BaseRepository;
import MAMS.Backend.repository.EquipmentRepository;
import MAMS.Backend.repository.EquipmentTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final BaseRepository baseRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final AuditLogService auditLogService;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            BaseRepository baseRepository,
            EquipmentRepository equipmentRepository,
            EquipmentTypeRepository equipmentTypeRepository,
            AuditLogService auditLogService) {
        this.assignmentRepository = assignmentRepository;
        this.baseRepository = baseRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.auditLogService = auditLogService;
    }

    public List<Assignment> getAll() {
        return assignmentRepository.findAll();
    }

    public Assignment getById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    @Transactional
    public Assignment create(Assignment assignment) {
        if (assignment.getQuantity() == null || assignment.getQuantity() <= 0) {
            throw new IllegalArgumentException("Assignment quantity must be positive");
        }
        Base base = baseRepository.findById(assignment.getBase().getId())
                .orElseThrow(() -> new IllegalArgumentException("Base not found"));
        EquipmentType equipmentType = equipmentTypeRepository
                .findById(assignment.getEquipmentType().getId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment type not found"));
        assignment.setBase(base);
        assignment.setEquipmentType(equipmentType);
        adjustStock(base, equipmentType, -assignment.getQuantity());
        Assignment savedAssignment = assignmentRepository.save(assignment);
        auditLogService.logCurrentUser(
                "CREATE", "Assignment", savedAssignment.getId(),
                savedAssignment.getQuantity() + " " + equipmentType.getName() + " assigned to "
                        + savedAssignment.getPersonnelName());
        return savedAssignment;
    }

    @Transactional
    public Assignment update(Long id, Assignment assignment) {
        if (assignment.getQuantity() == null || assignment.getQuantity() <= 0) {
            throw new IllegalArgumentException("Assignment quantity must be positive");
        }
        Assignment existing = getById(id);

        adjustStock(existing.getBase(), existing.getEquipmentType(), existing.getQuantity());
        Base base = baseRepository.findById(assignment.getBase().getId())
                .orElseThrow(() -> new IllegalArgumentException("Base not found"));
        EquipmentType equipmentType = equipmentTypeRepository
                .findById(assignment.getEquipmentType().getId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment type not found"));
        adjustStock(base, equipmentType, -assignment.getQuantity());

        existing.setBase(base);
        existing.setEquipmentType(equipmentType);
        existing.setPersonnelName(assignment.getPersonnelName());
        existing.setQuantity(assignment.getQuantity());
        existing.setAssignedDate(assignment.getAssignedDate());
        existing.setRemarks(assignment.getRemarks());

        Assignment savedAssignment = assignmentRepository.save(existing);
        auditLogService.logCurrentUser(
                "UPDATE", "Assignment", id, "Assignment updated for " + existing.getPersonnelName());
        return savedAssignment;
    }

    @Transactional
    public void delete(Long id) {
        Assignment assignment = getById(id);
        adjustStock(assignment.getBase(), assignment.getEquipmentType(), assignment.getQuantity());
        assignmentRepository.delete(assignment);
        auditLogService.logCurrentUser("DELETE", "Assignment", id, "Assignment " + id + " deleted");
    }

    public List<Assignment> getByBase(Long baseId) {
        return assignmentRepository.findByBaseId(baseId);
    }

    public List<Assignment> getByPersonnel(String personnelName) {
        return assignmentRepository.findByPersonnelNameContainingIgnoreCase(personnelName);
    }

    private void adjustStock(Base base, EquipmentType equipmentType, int quantityChange) {
        Equipment equipment = equipmentRepository
                .findFirstByBaseIdAndEquipmentTypeId(base.getId(), equipmentType.getId())
                .orElseGet(() -> new Equipment(equipmentType.getName(), null, equipmentType, base, 0));
        int updatedQuantity = equipment.getQuantity() + quantityChange;
        if (updatedQuantity < 0) {
            throw new IllegalArgumentException("Assignment exceeds available equipment");
        }
        equipment.setQuantity(updatedQuantity);
        equipmentRepository.save(equipment);
    }
}