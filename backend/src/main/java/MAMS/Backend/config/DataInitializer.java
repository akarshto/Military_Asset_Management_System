package MAMS.Backend.config;

import MAMS.Backend.entity.Base;
import MAMS.Backend.entity.Assignment;
import MAMS.Backend.entity.Equipment;
import MAMS.Backend.entity.EquipmentType;
import MAMS.Backend.entity.Expenditure;
import MAMS.Backend.entity.Purchase;
import MAMS.Backend.entity.Role;
import MAMS.Backend.entity.Transfer;
import MAMS.Backend.entity.User;
import MAMS.Backend.repository.BaseRepository;
import MAMS.Backend.repository.AssignmentRepository;
import MAMS.Backend.repository.EquipmentRepository;
import MAMS.Backend.repository.EquipmentTypeRepository;
import MAMS.Backend.repository.ExpenditureRepository;
import MAMS.Backend.repository.PurchaseRepository;
import MAMS.Backend.repository.TransferRepository;
import MAMS.Backend.repository.UserRepository;
import MAMS.Backend.service.AuditLogService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

        @Bean
        CommandLineRunner initializeData(
                        BaseRepository baseRepository,
                        EquipmentRepository equipmentRepository,
                        EquipmentTypeRepository equipmentTypeRepository,
                        PurchaseRepository purchaseRepository,
                        AssignmentRepository assignmentRepository,
                        TransferRepository transferRepository,
                        ExpenditureRepository expenditureRepository,
                        AuditLogService auditLogService,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {

                return args -> {

                        Base bangalore = baseRepository
                                        .findAll()
                                        .stream()
                                        .findFirst()
                                        .orElseGet(() -> baseRepository.save(
                                                        new Base(
                                                                        "Bangalore Base",
                                                                        "Bangalore")));

                        if (equipmentRepository.count() == 0 && purchaseRepository.count() == 0) {
                                EquipmentType radioType = equipmentTypeRepository
                                                .findByNameIgnoreCase("Radio")
                                                .orElseGet(() -> equipmentTypeRepository
                                                                .save(new EquipmentType("Radio")));
                                EquipmentType vehicleType = equipmentTypeRepository
                                                .findByNameIgnoreCase("Utility Vehicle")
                                                .orElseGet(() -> equipmentTypeRepository
                                                                .save(new EquipmentType("Utility Vehicle")));
                                EquipmentType armorType = equipmentTypeRepository
                                                .findByNameIgnoreCase("Body Armor")
                                                .orElseGet(() -> equipmentTypeRepository
                                                                .save(new EquipmentType("Body Armor")));

                                equipmentRepository.saveAll(java.util.List.of(
                                                new Equipment("Field Radio", "MAMS-RAD-001", radioType, bangalore, 24),
                                                new Equipment("Utility Vehicle", "MAMS-VEH-001", vehicleType, bangalore,
                                                                8),
                                                new Equipment("Body Armor Vest", "MAMS-ARM-001", armorType, bangalore,
                                                                60)));

                                purchaseRepository.saveAll(java.util.List.of(
                                                new Purchase(bangalore, radioType, 24,
                                                                LocalDateTime.now().minusMonths(3),
                                                                "Signal Supply Co."),
                                                new Purchase(bangalore, vehicleType, 8,
                                                                LocalDateTime.now().minusMonths(2),
                                                                "Fleet Logistics Ltd."),
                                                new Purchase(bangalore, armorType, 60,
                                                                LocalDateTime.now().minusMonths(1),
                                                                "Protective Gear Works")));
                        }

                        if (transferRepository.count() == 0) {
                                EquipmentType vehicleType = equipmentTypeRepository
                                                .findByNameIgnoreCase("Utility Vehicle")
                                                .orElse(null);
                                if (vehicleType != null) {
                                        Equipment sourceVehicles = equipmentRepository
                                                        .findFirstByBaseIdAndEquipmentTypeId(
                                                                        bangalore.getId(), vehicleType.getId())
                                                        .orElse(null);
                                        if (sourceVehicles != null && sourceVehicles.getQuantity() >= 6) {
                                                Base chennai = baseRepository.findAll().stream()
                                                                .filter(base -> base.getName()
                                                                                .equalsIgnoreCase("Chennai Base"))
                                                                .findFirst()
                                                                .orElseGet(() -> baseRepository.save(
                                                                                new Base("Chennai Base", "Chennai")));
                                                sourceVehicles.setQuantity(sourceVehicles.getQuantity() - 6);
                                                equipmentRepository.save(sourceVehicles);

                                                Equipment destinationVehicles = equipmentRepository
                                                                .findFirstByBaseIdAndEquipmentTypeId(
                                                                                chennai.getId(), vehicleType.getId())
                                                                .orElseGet(() -> new Equipment(
                                                                                vehicleType.getName(),
                                                                                null,
                                                                                vehicleType,
                                                                                chennai,
                                                                                0));
                                                destinationVehicles.setQuantity(
                                                                destinationVehicles.getQuantity() + 6);
                                                equipmentRepository.save(destinationVehicles);
                                                transferRepository.save(new Transfer(
                                                                bangalore,
                                                                chennai,
                                                                vehicleType,
                                                                6,
                                                                LocalDateTime.now().minusWeeks(2),
                                                                "Demo fleet allocation"));
                                        }
                                }
                        }

                        if (expenditureRepository.count() == 0) {
                                EquipmentType armorType = equipmentTypeRepository
                                                .findByNameIgnoreCase("Body Armor")
                                                .orElse(null);
                                if (armorType != null) {
                                        Equipment armor = equipmentRepository
                                                        .findFirstByBaseIdAndEquipmentTypeId(
                                                                        bangalore.getId(), armorType.getId())
                                                        .orElse(null);
                                        if (armor != null && armor.getQuantity() >= 4) {
                                                armor.setQuantity(armor.getQuantity() - 4);
                                                equipmentRepository.save(armor);
                                                expenditureRepository.save(new Expenditure(
                                                                bangalore,
                                                                armorType,
                                                                4,
                                                                LocalDateTime.now().minusWeeks(1),
                                                                "Training wear and damaged equipment"));
                                        }
                                }
                        }

                        if (assignmentRepository.count() == 0) {
                                EquipmentType radioType = equipmentTypeRepository
                                                .findByNameIgnoreCase("Radio")
                                                .orElse(null);
                                if (radioType != null) {
                                        Equipment radios = equipmentRepository
                                                        .findFirstByBaseIdAndEquipmentTypeId(
                                                                        bangalore.getId(), radioType.getId())
                                                        .orElse(null);
                                        if (radios != null && radios.getQuantity() >= 5) {
                                                radios.setQuantity(radios.getQuantity() - 5);
                                                equipmentRepository.save(radios);
                                                assignmentRepository.save(new Assignment(
                                                                bangalore,
                                                                radioType,
                                                                "Demo Squad Alpha",
                                                                5,
                                                                LocalDateTime.now().minusDays(10),
                                                                "Demo field assignment"));
                                        }
                                }
                        }

                        if (!userRepository.existsByUsername("admin")) {

                                userRepository.save(
                                                new User(
                                                                "admin",
                                                                passwordEncoder.encode("Admin@123"),
                                                                "admin@mams.com",
                                                                Role.ADMIN,
                                                                null));
                        }

                        if (!userRepository.existsByUsername("commander")) {

                                userRepository.save(
                                                new User(
                                                                "commander",
                                                                passwordEncoder.encode("Commander@123"),
                                                                "commander@mams.com",
                                                                Role.BASE_COMMANDER,
                                                                bangalore));
                        }

                        if (!userRepository.existsByUsername("logistics")) {

                                userRepository.save(
                                                new User(
                                                                "logistics",
                                                                passwordEncoder.encode("Logistics@123"),
                                                                "logistics@mams.com",
                                                                Role.LOGISTICS_OFFICER,
                                                                bangalore));
                        }

                        if (auditLogService.getAll().isEmpty()) {
                                auditLogService.log(
                                                "system",
                                                "INITIALIZE",
                                                "Demo Data",
                                                null,
                                                "Initialized sample inventory and movement records");
                        }
                };
        }
}