package pruebas.Infraestructure.Adapters;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pruebas.Domain.Model.User;
import pruebas.Domain.Ports.UserRepositoryPort;
import pruebas.Infraestructure.Entity.UserEntity;
import pruebas.Infraestructure.Repository.JpaUserRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor

public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository repository;

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<User> findByResetToken(String token) {
        return repository.findByResetToken(token).map(this::toDomain);
    }

    @Override
    public Optional<User> findByVerificationCode(String code) {
        return repository.findByVerificationCode(code).map(this::toDomain);
    }

    // ── Mapeos ─────────────────────────────────────────────────────────────────

    private User toDomain(UserEntity e) {
        return new User(
                e.getId(),
                e.getEmail(),
                e.getPassword(),
                e.getResetToken(),
                e.getResetTokenExpiry(),
                e.getVerificationCode(),
                e.getVerificationCodeExpiry(),
                e.isVerificationCodeUsed(),
                e.getFailedVerificationAttempts(),
                e.getLockedUntil(),
                e.isActive()
        );
    }

    private UserEntity toEntity(User u) {
        return UserEntity.builder()
                .id(u.getId())
                .email(u.getEmail())
                .password(u.getPassword())
                .resetToken(u.getResetToken())
                .resetTokenExpiry(u.getResetTokenExpiry())
                .verificationCode(u.getVerificationCode())
                .verificationCodeExpiry(u.getVerificationCodeExpiry())
                .verificationCodeUsed(u.isVerificationCodeUsed())
                .failedVerificationAttempts(u.getFailedVerificationAttempts())
                .lockedUntil(u.getLockedUntil())
                .active(u.isActive())
                .build();
    }
}