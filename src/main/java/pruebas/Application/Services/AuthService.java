package pruebas.Application.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pruebas.Domain.Model.User;
import pruebas.Domain.Ports.UserRepositoryPort;
import pruebas.Infraestructure.Config.JwtService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final AuditService auditService;

    @Value("${app.reset-token.expiry-minutes:15}")
    private int resetTokenExpiryMinutes;

    @Value("${app.verification.max-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.verification.lockout-minutes:30}")
    private int lockoutMinutes;

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));
        if (!user.isActive()) throw new RuntimeException("La cuenta está inactiva");
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new RuntimeException("Credenciales incorrectas");
        auditService.log(email, "LOGIN_EXITOSO", null);
        return jwtService.generateToken(user);
    }

    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            throw new RuntimeException("No se pudo completar el registro.");
        validatePasswordPolicy(user.getPassword()); // RB4
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        auditService.log(user.getEmail(), "REGISTRO_USUARIO", null);
        return userRepository.save(user);
    }

    public void generateResetToken(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes));
            userRepository.save(user);
            sendEmail(email, "Restablecimiento", frontendResetUrl + "?token=" + token);
            auditService.log(email, "RESET_TOKEN_GENERADO", null);
        });
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Enlace no válido o expirado"));
        if (LocalDateTime.now().isAfter(user.getResetTokenExpiry()))
            throw new RuntimeException("El enlace ha expirado.");
        validatePasswordPolicy(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        auditService.log(user.getEmail(), "CONTRASENA_RESTABLECIDA", null);
    }

    public void generateVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        checkLockout(user);
        String code = generateSixDigitCode();
        user.setVerificationCode(passwordEncoder.encode(code));
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes));
        user.setVerificationCodeUsed(false);
        userRepository.save(user);
        try {
            sendEmail(email, "Código de verificación", "Tu código: " + code);
        } catch (RuntimeException e) {
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);
            auditService.log(email, "CODIGO_ENVIO_FALLIDO", null);
            throw e;
        }
        auditService.log(email, "CODIGO_VERIFICACION_ENVIADO", null);
    }

    public void validateVerificationCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        checkLockout(user);
        if (user.getVerificationCode() == null)
            throw new RuntimeException("No hay código activo. Solicita uno nuevo.");
        if (LocalDateTime.now().isAfter(user.getVerificationCodeExpiry()))
            throw new RuntimeException("El código ha expirado.");
        if (user.isVerificationCodeUsed())
            throw new RuntimeException("Este código ya fue utilizado.");
        if (!passwordEncoder.matches(code, user.getVerificationCode())) {
            int attempts = user.getFailedVerificationAttempts() + 1;
            user.setFailedVerificationAttempts(attempts);
            if (attempts >= maxFailedAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
                userRepository.save(user);
                auditService.log(email, "CUENTA_BLOQUEADA_INTENTOS", "intentos: " + attempts);
                throw new RuntimeException("Cuenta bloqueada temporalmente.");
            }
            userRepository.save(user);
            throw new RuntimeException("Código incorrecto. Restantes: " + (maxFailedAttempts - attempts));
        }
        user.setVerificationCodeUsed(true);
        user.setFailedVerificationAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        auditService.log(email, "VERIFICACION_EXITOSA", null);
    }

    public String changePassword(String email, String current, String newPass) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(current, user.getPassword()))
            throw new RuntimeException("La contraseña actual es incorrecta");
        validatePasswordPolicy(newPass);
        user.setPassword(passwordEncoder.encode(newPass));
        user.setVerificationCode(null); // RB5
        userRepository.save(user);
        auditService.log(email, "CONTRASENA_CAMBIADA", null);
        return jwtService.generateToken(user);
    }

    private void validatePasswordPolicy(String p) {
        if (p == null || p.length() < 12) throw new RuntimeException("Mínimo 12 caracteres");
        if (!p.matches(".*[A-Z].*")) throw new RuntimeException("Falta mayúscula");
        if (!p.matches(".*[a-z].*")) throw new RuntimeException("Falta minúscula");
        if (!p.matches(".*[0-9].*")) throw new RuntimeException("Falta número");
        if (!p.matches(".*[!@#$%^&*].*")) throw new RuntimeException("Falta símbolo");
    }

    private void checkLockout(User user) {
        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil()))
            throw new RuntimeException("Cuenta bloqueada temporalmente.");
    }

    private String generateSixDigitCode() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setTo(to); m.setSubject(subject); m.setText(body);
            mailSender.send(m);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el correo. Intenta de nuevo.");
        }
    }
}