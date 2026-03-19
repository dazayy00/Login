package pruebas.Domain.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String email;
    private String password;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    private String verificationCode;
    private LocalDateTime verificationCodeExpiry;
    private boolean verificationCodeUsed;

    private int failedVerificationAttempts;
    private LocalDateTime lockedUntil;

    private boolean active;
    private int sessionVersion;

}