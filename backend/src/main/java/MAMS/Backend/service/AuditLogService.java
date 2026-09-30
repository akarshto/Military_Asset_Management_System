package MAMS.Backend.service;

import MAMS.Backend.entity.AuditLog;
import MAMS.Backend.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog log(
            String username,
            String action,
            String entityType,
            Long entityId,
            String details) {

        AuditLog log = new AuditLog(
                username,
                action,
                entityType,
                entityId,
                LocalDateTime.now(),
                details);

        return auditLogRepository.save(log);
    }

    public AuditLog logCurrentUser(
            String action,
            String entityType,
            Long entityId,
            String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())
                        ? "system"
                        : authentication.getName();
        return log(username, action, entityType, entityId, details);
    }

    public List<AuditLog> getAll() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }

    public List<AuditLog> getByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType);
    }
}