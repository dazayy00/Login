package pruebas.Domain.Model;

public class User {

    private Long id;
    private String email;
    private String password;
    private String resetToken;
    private java.time.LocalDateTime resetTokenExpiry;
    private String verificationCode;
    private java.time.LocalDateTime verificationCodeExpiry;
    private boolean verificationCodeUsed;
    private int failedVerificationAttempts;
    private java.time.LocalDateTime lockedUntil;
    private boolean active;
    private int sessionVersion;

    public User() {}

    public User(Long id, String email, String password,
                String resetToken, java.time.LocalDateTime resetTokenExpiry,
                String verificationCode, java.time.LocalDateTime verificationCodeExpiry,
                boolean verificationCodeUsed,
                int failedVerificationAttempts, java.time.LocalDateTime lockedUntil,
                boolean active, int sessionVersion) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.resetToken = resetToken;
        this.resetTokenExpiry = resetTokenExpiry;
        this.verificationCode = verificationCode;
        this.verificationCodeExpiry = verificationCodeExpiry;
        this.verificationCodeUsed = verificationCodeUsed;
        this.failedVerificationAttempts = failedVerificationAttempts;
        this.lockedUntil = lockedUntil;
        this.active = active;
        this.sessionVersion = sessionVersion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public java.time.LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(java.time.LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public java.time.LocalDateTime getVerificationCodeExpiry() { return verificationCodeExpiry; }
    public void setVerificationCodeExpiry(java.time.LocalDateTime verificationCodeExpiry) { this.verificationCodeExpiry = verificationCodeExpiry; }

    public boolean isVerificationCodeUsed() { return verificationCodeUsed; }
    public void setVerificationCodeUsed(boolean verificationCodeUsed) { this.verificationCodeUsed = verificationCodeUsed; }

    public int getFailedVerificationAttempts() { return failedVerificationAttempts; }
    public void setFailedVerificationAttempts(int failedVerificationAttempts) { this.failedVerificationAttempts = failedVerificationAttempts; }

    public java.time.LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(java.time.LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getSessionVersion() { return sessionVersion; }
    public void setSessionVersion(int sessionVersion) { this.sessionVersion = sessionVersion; }
}
