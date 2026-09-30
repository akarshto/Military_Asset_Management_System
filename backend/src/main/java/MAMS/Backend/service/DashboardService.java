package MAMS.Backend.service;

import MAMS.Backend.entity.Assignment;
import MAMS.Backend.entity.Equipment;
import MAMS.Backend.entity.Expenditure;
import MAMS.Backend.entity.Purchase;
import MAMS.Backend.entity.Transfer;
import MAMS.Backend.repository.AssignmentRepository;
import MAMS.Backend.repository.EquipmentRepository;
import MAMS.Backend.repository.ExpenditureRepository;
import MAMS.Backend.repository.PurchaseRepository;
import MAMS.Backend.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

        private final PurchaseRepository purchaseRepository;
        private final TransferRepository transferRepository;
        private final AssignmentRepository assignmentRepository;
        private final ExpenditureRepository expenditureRepository;
        private final EquipmentRepository equipmentRepository;

        public DashboardService(
                        PurchaseRepository purchaseRepository,
                        TransferRepository transferRepository,
                        AssignmentRepository assignmentRepository,
                        ExpenditureRepository expenditureRepository,
                        EquipmentRepository equipmentRepository) {

                this.purchaseRepository = purchaseRepository;
                this.transferRepository = transferRepository;
                this.assignmentRepository = assignmentRepository;
                this.expenditureRepository = expenditureRepository;
                this.equipmentRepository = equipmentRepository;
        }

        public Map<String, Object> getDashboard(Long baseId, Long equipmentTypeId) {

                List<Purchase> purchases;
                List<Transfer> incomingTransfers;
                List<Transfer> outgoingTransfers;
                List<Assignment> assignments;
                List<Expenditure> expenditures;

                if (baseId != null) {

                        purchases = purchaseRepository.findByBaseId(baseId);
                        incomingTransfers = transferRepository.findByToBaseId(baseId);
                        outgoingTransfers = transferRepository.findByFromBaseId(baseId);
                        assignments = assignmentRepository.findByBaseId(baseId);
                        expenditures = expenditureRepository.findByBaseId(baseId);

                } else {

                        purchases = purchaseRepository.findAll();
                        incomingTransfers = transferRepository.findAll();
                        outgoingTransfers = transferRepository.findAll();
                        assignments = assignmentRepository.findAll();
                        expenditures = expenditureRepository.findAll();
                }

                long purchaseQuantity = purchases.stream()
                                .filter(p -> equipmentTypeId == null ||
                                                p.getEquipmentType().getId().equals(equipmentTypeId))
                                .mapToLong(p -> p.getQuantity())
                                .sum();

                long transferInQuantity = incomingTransfers.stream()
                                .filter(t -> equipmentTypeId == null ||
                                                t.getEquipmentType().getId().equals(equipmentTypeId))
                                .mapToLong(t -> t.getQuantity())
                                .sum();

                long transferOutQuantity = outgoingTransfers.stream()
                                .filter(t -> equipmentTypeId == null ||
                                                t.getEquipmentType().getId().equals(equipmentTypeId))
                                .mapToLong(t -> t.getQuantity())
                                .sum();

                long assignedQuantity = assignments.stream()
                                .filter(a -> equipmentTypeId == null ||
                                                a.getEquipmentType().getId().equals(equipmentTypeId))
                                .mapToLong(a -> a.getQuantity())
                                .sum();

                long expendedQuantity = expenditures.stream()
                                .filter(e -> equipmentTypeId == null ||
                                                e.getEquipmentType().getId().equals(equipmentTypeId))
                                .mapToLong(e -> e.getQuantity())
                                .sum();

                List<Equipment> equipment = baseId == null
                                ? equipmentRepository.findAll()
                                : equipmentRepository.findByBaseId(baseId);
                long equipmentUnits = equipment.stream()
                                .filter(item -> equipmentTypeId == null ||
                                                item.getEquipmentType().getId().equals(equipmentTypeId))
                                .mapToLong(item -> item.getQuantity())
                                .sum();

                long netMovement = purchaseQuantity
                                + transferInQuantity
                                - transferOutQuantity;

                Map<String, Object> result = new HashMap<>();

                result.put("openingBalance", 0);
                result.put("purchases", purchaseQuantity);
                result.put("transferIn", transferInQuantity);
                result.put("transferOut", transferOutQuantity);
                result.put("netMovement", netMovement);
                result.put("assigned", assignedQuantity);
                result.put("expended", expendedQuantity);

                long closingBalance = netMovement
                                - assignedQuantity
                                - expendedQuantity;

                result.put("closingBalance", closingBalance);
                result.put("equipmentUnits", equipmentUnits);

                return result;
        }
}