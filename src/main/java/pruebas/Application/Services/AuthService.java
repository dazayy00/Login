package pruebas.Application.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pruebas.Domain.Model.User;
import pruebas.Domain.Ports.UserRepositoryPort;
import pruebas.Infraestructure.Config.JwtService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        return jwtService.generateToken(user);
    }

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email no registrado"));
        user.setResetToken(UUID.randomUUID().toString());
        userRepository.save(user);

    }
}
