package MAMS.Backend.controller;

import MAMS.Backend.dto.PurchaseRequest;
import MAMS.Backend.entity.Purchase;
import MAMS.Backend.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Purchase createPurchase(
            @Valid @RequestBody PurchaseRequest request) {

        return purchaseService.createPurchase(request);
    }

    @GetMapping
    public List<Purchase> getPurchases(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId) {

        if (baseId != null) {
            return purchaseService.getPurchasesByBase(baseId);
        }

        if (equipmentTypeId != null) {
            return purchaseService
                    .getPurchasesByEquipmentType(equipmentTypeId);
        }

        return purchaseService.getAllPurchases();
    }

    @GetMapping("/{id}")
    public Purchase getPurchase(@PathVariable Long id) {
        return purchaseService.getPurchase(id);
    }
}