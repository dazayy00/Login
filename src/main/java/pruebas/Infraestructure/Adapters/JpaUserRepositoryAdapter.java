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

    private User toDomain(UserEntity entity) {

        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .resetToken(entity.getResetToken())
                .resetTokenExpiry(entity.getResetTokenExpiry())
                .verificationCode(entity.getVerificationCode())
                .verificationCodeExpiry(entity.getVerificationCodeExpiry())
                .verificationCodeUsed(entity.isVerificationCodeUsed())
                .failedVerificationAttempts(entity.getFailedVerificationAttempts())
                .lockedUntil(entity.getLockedUntil())
                .active(entity.isActive())
                .sessionVersion(entity.getSessionVersion())
                .build();
    }

    private UserEntity toEntity(User user) {

        return UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .resetToken(user.getResetToken())
                .resetTokenExpiry(user.getResetTokenExpiry())
                .verificationCode(user.getVerificationCode())
                .verificationCodeExpiry(user.getVerificationCodeExpiry())
                .verificationCodeUsed(user.isVerificationCodeUsed())
                .failedVerificationAttempts(user.getFailedVerificationAttempts())
                .lockedUntil(user.getLockedUntil())
                .active(user.isActive())
                .sessionVersion(user.getSessionVersion())
                .build();
    }
}