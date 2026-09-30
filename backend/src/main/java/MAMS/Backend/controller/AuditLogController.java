package MAMS.Backend.controller;

import MAMS.Backend.entity.AuditLog;
import MAMS.Backend.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLog> getAll() {
        return auditLogService.getAll();
    }

    @GetMapping("/user/{username}")
    public List<AuditLog> getByUsername(
            @PathVariable String username) {

        return auditLogService.getByUsername(username);
    }

    @GetMapping("/entity/{entityType}")
    public List<AuditLog> getByEntityType(
            @PathVariable String entityType) {

        return auditLogService.getByEntityType(entityType);
    }
}