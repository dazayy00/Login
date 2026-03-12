package pruebas.Infraestructure.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pruebas.Application.Services.AuthService;
import pruebas.Domain.Model.User;
import pruebas.Infraestructure.DTO.LoginRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permite acceso desde Web y Móvil
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest req) {
        String token = authService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgot(@RequestParam String email) {
        authService.generateResetToken(email);
        return ResponseEntity.ok("Instrucciones enviadas al correo");
    }
}
