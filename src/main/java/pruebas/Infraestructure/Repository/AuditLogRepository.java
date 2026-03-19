package pruebas.Infraestructure.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pruebas.Infraestructure.Entity.AuditLogEntity;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findByUserEmailOrderByTimestampDesc(String email);
    List<AuditLogEntity> findByActionOrderByTimestampDesc(String action);
}