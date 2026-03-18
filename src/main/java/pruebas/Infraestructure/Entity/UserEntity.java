package pruebas.Infraestructure.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    private String verificationCode;
    private LocalDateTime verificationCodeExpiry;
    private boolean verificationCodeUsed;

    @Builder.Default
    private int failedVerificationAttempts = 0;
    private LocalDateTime lockedUntil;

    @Builder.Default
    private boolean active = true;
}