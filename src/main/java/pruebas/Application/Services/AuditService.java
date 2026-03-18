package pruebas.Application.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(String userEmail, String action, String detail) {
        String timestamp = LocalDateTime.now().format(FMT);
        String msg = String.format("[AUDIT] %s | usuario=%s | accion=%s%s",
                timestamp,
                userEmail,
                action,
                detail != null ? " | detalle=" + detail : "");
        log.info(msg);
    }
}
