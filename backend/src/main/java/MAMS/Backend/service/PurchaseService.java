package MAMS.Backend.service;

import MAMS.Backend.dto.PurchaseRequest;
import MAMS.Backend.entity.Base;
import MAMS.Backend.entity.Equipment;
import MAMS.Backend.entity.EquipmentType;
import MAMS.Backend.entity.Purchase;
import MAMS.Backend.repository.BaseRepository;
import MAMS.Backend.repository.EquipmentRepository;
import MAMS.Backend.repository.EquipmentTypeRepository;
import MAMS.Backend.repository.PurchaseRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final BaseRepository baseRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentRepository equipmentRepository;
    private final AuditLogService auditLogService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            BaseRepository baseRepository,
            EquipmentTypeRepository equipmentTypeRepository,
            EquipmentRepository equipmentRepository,
            AuditLogService auditLogService) {

        this.purchaseRepository = purchaseRepository;
        this.baseRepository = baseRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.equipmentRepository = equipmentRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Purchase createPurchase(PurchaseRequest request) {

        Base base = baseRepository.findById(request.getBaseId())
                .orElseThrow(() -> new RuntimeException("Base not found: " + request.getBaseId()));

        EquipmentType equipmentType = equipmentTypeRepository.findById(request.getEquipmentTypeId())
                .orElseThrow(() -> new RuntimeException(
                        "Equipment type not found: "
                                + request.getEquipmentTypeId()));

        Purchase purchase = new Purchase();

        purchase.setBase(base);
        purchase.setEquipmentType(equipmentType);
        purchase.setQuantity(request.getQuantity());
        purchase.setPurchaseDate(request.getPurchaseDate());
        purchase.setSupplier(request.getSupplier());

        Purchase savedPurchase = purchaseRepository.save(purchase);
        Equipment equipment = equipmentRepository
                .findFirstByBaseIdAndEquipmentTypeId(base.getId(), equipmentType.getId())
                .orElseGet(() -> new Equipment(
                        equipmentType.getName(),
                        null,
                        equipmentType,
                        base,
                        0));
        equipment.setQuantity(equipment.getQuantity() + request.getQuantity());
        equipmentRepository.save(equipment);
        auditLogService.logCurrentUser(
                "CREATE", "Purchase", savedPurchase.getId(),
                request.getQuantity() + " " + equipmentType.getName() + " at " + base.getName());

        return savedPurchase;
    }

    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    public Purchase getPurchase(Long id) {

        return purchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found: " + id));
    }

    public List<Purchase> getPurchasesByBase(Long baseId) {
        return purchaseRepository.findByBaseId(baseId);
    }

    public List<Purchase> getPurchasesByEquipmentType(Long equipmentTypeId) {
        return purchaseRepository.findByEquipmentTypeId(equipmentTypeId);
    }
}