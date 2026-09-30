package MAMS.Backend.controller;

import MAMS.Backend.entity.Expenditure;
import MAMS.Backend.service.ExpenditureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenditures")
@CrossOrigin
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    public ExpenditureController(ExpenditureService expenditureService) {
        this.expenditureService = expenditureService;
    }

    @GetMapping
    public List<Expenditure> getAll() {
        return expenditureService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expenditure> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expenditureService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Expenditure> create(
            @RequestBody Expenditure expenditure) {

        return ResponseEntity.ok(
                expenditureService.create(expenditure)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenditureService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/base/{baseId}")
    public List<Expenditure> getByBase(@PathVariable Long baseId) {
        return expenditureService.getByBase(baseId);
    }

    @GetMapping("/equipment/{equipmentTypeId}")
    public List<Expenditure> getByEquipmentType(
            @PathVariable Long equipmentTypeId) {

        return expenditureService.getByEquipmentType(equipmentTypeId);
    }
}