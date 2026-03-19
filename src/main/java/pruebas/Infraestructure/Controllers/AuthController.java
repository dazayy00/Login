package pruebas.Infraestructure.Controllers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pruebas.Application.DTO.LoginReq;
import pruebas.Application.DTO.RegisterReq;
import pruebas.Application.Services.AuthService;
import pruebas.Domain.Model.User;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody LoginReq req) {
        return ResponseEntity.ok(Map.of("token", authService.login(req.getEmail(), req.getPassword())));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String,String>> register(@RequestBody User user) {
        authService.register(user);
        return ResponseEntity.ok(Map.of("message", "Usuario registrado exitosamente"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String,String>> forgotPassword(@RequestParam String email) {
        authService.generateResetToken(email);
        return ResponseEntity.ok(Map.of("message",
                "Si el correo está registrado, recibirás instrucciones en breve."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String,String>> resetPassword(@RequestBody ResetPasswordReq req) {
        authService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente"));
    }

    @PostMapping("/verify/send-code")
    public ResponseEntity<Map<String,String>> sendCode(@AuthenticationPrincipal UserDetails ud) {
        authService.generateVerificationCode(ud.getUsername());
        return ResponseEntity.ok(Map.of("message", "Código enviado a tu correo"));
    }

    @PostMapping("/verify/validate-code")
    public ResponseEntity<Map<String,String>> validateCode(
            @AuthenticationPrincipal UserDetails ud, @RequestBody ValidateCodeReq req) {
        authService.validateVerificationCode(ud.getUsername(), req.getCode());
        return ResponseEntity.ok(Map.of("message", "Verificación exitosa. Puedes continuar."));
    }

    @PostMapping("/verify/change-password")
    public ResponseEntity<Map<String,String>> changePassword(
            @AuthenticationPrincipal UserDetails ud, @RequestBody ChangePasswordReq req) {
        String token = authService.changePassword(ud.getUsername(),
                req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada", "token", token));
    }

    @Data static class LoginReq { private String email, password; }
    @Data static class ResetPasswordReq { private String token, newPassword; }
    @Data static class ValidateCodeReq { private String code; }
    @Data static class ChangePasswordReq { private String currentPassword, newPassword; }
}