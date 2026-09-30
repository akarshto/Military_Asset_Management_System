package MAMS.Backend.controller;

import MAMS.Backend.entity.Base;
import MAMS.Backend.repository.BaseRepository;
import MAMS.Backend.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bases")
public class BaseController {

    private final BaseRepository baseRepository;
    private final AuditLogService auditLogService;

    public BaseController(BaseRepository baseRepository, AuditLogService auditLogService) {
        this.baseRepository = baseRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<Base> getAllBases() {
        return baseRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Base> getBaseById(@PathVariable Long id) {
        return baseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Base createBase(@RequestBody Base base) {
        Base savedBase = baseRepository.save(base);
        auditLogService.logCurrentUser(
                "CREATE", "Base", savedBase.getId(), savedBase.getName());
        return savedBase;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Base> updateBase(
            @PathVariable Long id,
            @RequestBody Base baseDetails) {

        return baseRepository.findById(id)
                .map(base -> {
                    base.setName(baseDetails.getName());
                    base.setLocation(baseDetails.getLocation());
                    Base savedBase = baseRepository.save(base);
                    auditLogService.logCurrentUser(
                            "UPDATE", "Base", id, savedBase.getName());
                    return ResponseEntity.ok(savedBase);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBase(@PathVariable Long id) {

        if (!baseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Base base = baseRepository.findById(id).orElseThrow();
        baseRepository.delete(base);
        auditLogService.logCurrentUser("DELETE", "Base", id, base.getName());
        return ResponseEntity.noContent().build();
    }
}
