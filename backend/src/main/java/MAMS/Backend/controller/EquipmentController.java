package MAMS.Backend.controller;

import MAMS.Backend.dto.EquipmentRequest;
import MAMS.Backend.entity.Equipment;
import MAMS.Backend.entity.EquipmentType;
import MAMS.Backend.repository.EquipmentTypeRepository;
import MAMS.Backend.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final EquipmentTypeRepository equipmentTypeRepository;

    public EquipmentController(
            EquipmentService equipmentService,
            EquipmentTypeRepository equipmentTypeRepository) {
        this.equipmentService = equipmentService;
        this.equipmentTypeRepository = equipmentTypeRepository;
    }

    @GetMapping
    public List<Equipment> getAllEquipment() {
        return equipmentService.getAllEquipment();
    }

    @GetMapping("/types")
    public List<EquipmentType> getEquipmentTypes() {
        return equipmentTypeRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipment createEquipment(@Valid @RequestBody EquipmentRequest request) {
        return equipmentService.createEquipment(request);
    }
}