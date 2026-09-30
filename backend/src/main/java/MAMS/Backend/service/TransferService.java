package MAMS.Backend.service;

import MAMS.Backend.entity.Transfer;
import MAMS.Backend.entity.Base;
import MAMS.Backend.entity.Equipment;
import MAMS.Backend.entity.EquipmentType;
import MAMS.Backend.repository.BaseRepository;
import MAMS.Backend.repository.EquipmentRepository;
import MAMS.Backend.repository.EquipmentTypeRepository;
import MAMS.Backend.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final BaseRepository baseRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final AuditLogService auditLogService;

    public TransferService(
            TransferRepository transferRepository,
            BaseRepository baseRepository,
            EquipmentRepository equipmentRepository,
            EquipmentTypeRepository equipmentTypeRepository,
            AuditLogService auditLogService) {
        this.transferRepository = transferRepository;
        this.baseRepository = baseRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.auditLogService = auditLogService;
    }

    public List<Transfer> getAll() {
        return transferRepository.findAll();
    }

    public Transfer getById(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
    }

    @Transactional
    public Transfer create(Transfer request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be positive");
        }

        Base fromBase = baseRepository.findById(request.getFromBase().getId())
                .orElseThrow(() -> new IllegalArgumentException("Source base not found"));
        Base toBase = baseRepository.findById(request.getToBase().getId())
                .orElseThrow(() -> new IllegalArgumentException("Destination base not found"));
        if (fromBase.getId().equals(toBase.getId())) {
            throw new IllegalArgumentException("Source and destination bases cannot be the same");
        }
        EquipmentType equipmentType = equipmentTypeRepository
                .findById(request.getEquipmentType().getId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment type not found"));
        Equipment source = equipmentRepository
                .findFirstByBaseIdAndEquipmentTypeId(fromBase.getId(), equipmentType.getId())
                .orElseThrow(() -> new IllegalArgumentException("No matching equipment at source base"));
        if (source.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Transfer quantity exceeds available equipment");
        }

        Equipment destination = equipmentRepository
                .findFirstByBaseIdAndEquipmentTypeId(toBase.getId(), equipmentType.getId())
                .orElseGet(() -> new Equipment(equipmentType.getName(), null, equipmentType, toBase, 0));
        source.setQuantity(source.getQuantity() - request.getQuantity());
        destination.setQuantity(destination.getQuantity() + request.getQuantity());
        equipmentRepository.save(source);
        equipmentRepository.save(destination);

        request.setFromBase(fromBase);
        request.setToBase(toBase);
        request.setEquipmentType(equipmentType);
        Transfer savedTransfer = transferRepository.save(request);
        auditLogService.logCurrentUser(
                "CREATE", "Transfer", savedTransfer.getId(),
                request.getQuantity() + " " + equipmentType.getName() + " from "
                        + fromBase.getName() + " to " + toBase.getName());
        return savedTransfer;
    }

    @Transactional
    public void delete(Long id) {
        Transfer transfer = getById(id);
        Equipment source = equipmentRepository
                .findFirstByBaseIdAndEquipmentTypeId(
                        transfer.getFromBase().getId(), transfer.getEquipmentType().getId())
                .orElseGet(() -> new Equipment(
                        transfer.getEquipmentType().getName(), null, transfer.getEquipmentType(),
                        transfer.getFromBase(), 0));
        Equipment destination = equipmentRepository
                .findFirstByBaseIdAndEquipmentTypeId(
                        transfer.getToBase().getId(), transfer.getEquipmentType().getId())
                .orElseThrow(() -> new IllegalStateException("Transferred equipment is missing"));
        if (destination.getQuantity() < transfer.getQuantity()) {
            throw new IllegalStateException("Cannot reverse transfer because destination stock has changed");
        }
        source.setQuantity(source.getQuantity() + transfer.getQuantity());
        destination.setQuantity(destination.getQuantity() - transfer.getQuantity());
        equipmentRepository.save(source);
        equipmentRepository.save(destination);
        transferRepository.delete(transfer);
        auditLogService.logCurrentUser(
                "DELETE", "Transfer", id, "Transfer " + id + " reversed");
    }

    public List<Transfer> getOutgoing(Long baseId) {
        return transferRepository.findByFromBaseId(baseId);
    }

    public List<Transfer> getIncoming(Long baseId) {
        return transferRepository.findByToBaseId(baseId);
    }

    public List<Transfer> getByEquipmentType(Long equipmentTypeId) {
        return transferRepository.findByEquipmentTypeId(equipmentTypeId);
    }
}