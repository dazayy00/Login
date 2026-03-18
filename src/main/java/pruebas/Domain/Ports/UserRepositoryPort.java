package pruebas.Domain.Ports;

import pruebas.Domain.Model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String token);
    Optional<User> findByVerificationCode(String code);
}
