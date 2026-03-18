package pruebas.Infraestructure.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pruebas.Infraestructure.Entity.UserEntity;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByResetToken(String token);

    Optional<UserEntity> findByVerificationCode(String code);
}
