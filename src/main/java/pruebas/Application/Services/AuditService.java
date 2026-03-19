package pruebas.Application.Services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pruebas.Infraestructure.Entity.AuditLogEntity;
import pruebas.Infraestructure.Repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditLogRepository auditLogRepository;

    public void log(String userEmail, String action, String detail) {
        LocalDateTime now = LocalDateTime.now();

        log.info("[AUDIT] {} | usuario={} | accion={}{}", now.format(FMT),
                userEmail, action, detail != null ? " | " + detail : "");

        try {
            auditLogRepository.save(AuditLogEntity.builder()
                    .timestamp(now)
                    .userEmail(userEmail)
                    .action(action)
                    .detail(detail)
                    .build());
        } catch (Exception e) {
            log.error("[AUDIT-ERROR] No se pudo persistir: usuario={} accion={}", userEmail, action, e);
        }
    }
}
