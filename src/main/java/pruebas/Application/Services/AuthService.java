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

    @Value("${app.frontend.reset-url:http://localhost:4200/reset-password}")
    private String frontendResetUrl;

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        if (!user.isActive()) {
            throw new RuntimeException("La cuenta está inactiva o bloqueada");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        auditService.log(email, "LOGIN_EXITOSO", null);
        return jwtService.generateToken(user);
    }

    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("No se pudo completar el registro. Intente con otro correo.");
        }

        validatePasswordPolicy(user.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        user.setFailedVerificationAttempts(0);

        User saved = userRepository.save(user);
        auditService.log(user.getEmail(), "REGISTRO_USUARIO", null);
        return saved;
    }

    public void generateResetToken(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes)); // RB1
            userRepository.save(user);

            String link = frontendResetUrl + "?token=" + token;
            sendEmail(email,
                    "Restablecimiento de contraseña",
                    "Haz clic en el siguiente enlace para restablecer tu contraseña:\n" + link
                            + "\n\nEste enlace expira en " + resetTokenExpiryMinutes + " minutos.");

            auditService.log(email, "RESET_TOKEN_GENERADO", null);
        });
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("El enlace no es válido o ha expirado"));

        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new RuntimeException("El enlace ha expirado. Solicita uno nuevo.");
        }

        validatePasswordPolicy(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));

        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        user.setFailedVerificationAttempts(0);
        user.setLockedUntil(null);

        userRepository.save(user);
        auditService.log(user.getEmail(), "CONTRASENA_RESTABLECIDA", "Vía token de reset");
    }

    public void generateVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        checkLockout(user);

        String code = generateSixDigitCode();
        user.setVerificationCode(passwordEncoder.encode(code)); // guardado como hash (no en claro)
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes)); // RB1
        user.setVerificationCodeUsed(false);
        userRepository.save(user);

        sendEmail(email,
                "Código de verificación",
                "Tu código de verificación es: " + code
                        + "\n\nExpira en " + resetTokenExpiryMinutes + " minutos. No lo compartas.");

        auditService.log(email, "CODIGO_VERIFICACION_ENVIADO", null);
    }

    public void validateVerificationCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        checkLockout(user);

        if (user.getVerificationCode() == null || user.getVerificationCodeExpiry() == null) {
            throw new RuntimeException("No hay un código de verificación activo. Solicita uno nuevo.");
        }

        if (LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            auditService.log(email, "CODIGO_EXPIRADO", null);
            throw new RuntimeException("El código ha expirado. Solicita uno nuevo.");
        }

        if (user.isVerificationCodeUsed()) {
            throw new RuntimeException("Este código ya fue utilizado. Solicita uno nuevo.");
        }

        if (!passwordEncoder.matches(code, user.getVerificationCode())) {
            int attempts = user.getFailedVerificationAttempts() + 1;
            user.setFailedVerificationAttempts(attempts);

            if (attempts >= maxFailedAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
                userRepository.save(user);
                auditService.log(email, "CUENTA_BLOQUEADA_INTENTOS", "Intentos: " + attempts);
                throw new RuntimeException("Cuenta bloqueada temporalmente por múltiples intentos fallidos.");
            }

            userRepository.save(user);
            auditService.log(email, "CODIGO_INCORRECTO", "Intento " + attempts + " de " + maxFailedAttempts);
            throw new RuntimeException("Código incorrecto. Intentos restantes: " + (maxFailedAttempts - attempts));
        }

        user.setVerificationCodeUsed(true);
        user.setFailedVerificationAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        auditService.log(email, "VERIFICACION_EXITOSA", null);
    }

    public String changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        validatePasswordPolicy(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        user.setVerificationCodeUsed(false);

        userRepository.save(user);
        auditService.log(email, "CONTRASENA_CAMBIADA", "Acción sensible completada");

        return jwtService.generateToken(user);
    }

    private void validatePasswordPolicy(String password) {
        if (password == null || password.length() < 12) {
            throw new RuntimeException("La contraseña debe tener al menos 12 caracteres");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("La contraseña debe contener al menos una letra mayúscula");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("La contraseña debe contener al menos una letra minúscula");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new RuntimeException("La contraseña debe contener al menos un número");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new RuntimeException("La contraseña debe contener al menos un símbolo especial");
        }
    }

    private void checkLockout(User user) {
        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
            throw new RuntimeException("Cuenta bloqueada temporalmente. Intenta de nuevo más tarde.");
        }
    }

    private String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
