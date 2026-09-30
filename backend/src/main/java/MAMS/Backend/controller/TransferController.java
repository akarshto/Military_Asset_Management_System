package MAMS.Backend.controller;

import MAMS.Backend.entity.Transfer;
import MAMS.Backend.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public List<Transfer> getAll() {
        return transferService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transfer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Transfer> create(
            @RequestBody Transfer transfer) {

        return ResponseEntity.ok(transferService.create(transfer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transferService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/outgoing/{baseId}")
    public List<Transfer> getOutgoing(@PathVariable Long baseId) {
        return transferService.getOutgoing(baseId);
    }

    @GetMapping("/incoming/{baseId}")
    public List<Transfer> getIncoming(@PathVariable Long baseId) {
        return transferService.getIncoming(baseId);
    }

    @GetMapping("/equipment/{equipmentTypeId}")
    public List<Transfer> getByEquipmentType(
            @PathVariable Long equipmentTypeId) {

        return transferService.getByEquipmentType(equipmentTypeId);
    }
}