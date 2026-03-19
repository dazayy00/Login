package pruebas.Application.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pruebas.Domain.Model.User;
import pruebas.Domain.Ports.UserRepositoryPort;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepositoryPort userRepository;
    private final AuditService auditService;

    public void changeEmail(String currentEmail, String newEmail) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (userRepository.findByEmail(newEmail).isPresent())
            throw new RuntimeException("No se pudo actualizar el correo.");
        user.setEmail(newEmail);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        user.setVerificationCodeUsed(false);
        userRepository.save(user);
        auditService.log(currentEmail, "CORREO_ACTUALIZADO", null);
    }

    public void closeAllSessions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setSessionVersion(user.getSessionVersion() + 1);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        user.setVerificationCodeUsed(false);
        userRepository.save(user);
        auditService.log(email, "SESIONES_CERRADAS", null);
    }

    public void unlockAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setLockedUntil(null);
        user.setFailedVerificationAttempts(0);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        user.setVerificationCodeUsed(false);
        userRepository.save(user);
        auditService.log(email, "CUENTA_DESBLOQUEADA", null);
    }
}
