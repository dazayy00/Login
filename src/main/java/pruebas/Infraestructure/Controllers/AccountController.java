package pruebas.Infraestructure.Controllers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pruebas.Application.Services.AccountService;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AccountController {
    private final AccountService accountService;

    @PutMapping("/email")
    public ResponseEntity<Map<String,String>> changeEmail(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody ChangeEmailReq req) {
        accountService.changeEmail(ud.getUsername(), req.getNewEmail());
        return ResponseEntity.ok(Map.of("message", "Correo actualizado exitosamente"));
    }

    @PostMapping("/close-sessions")
    public ResponseEntity<Map<String,String>> closeSessions(@AuthenticationPrincipal UserDetails ud) {
        accountService.closeAllSessions(ud.getUsername());
        return ResponseEntity.ok(Map.of("message", "Todas las sesiones han sido cerradas"));
    }

    @PostMapping("/unlock")
    public ResponseEntity<Map<String,String>> unlockAccount(@AuthenticationPrincipal UserDetails ud) {
        accountService.unlockAccount(ud.getUsername());
        return ResponseEntity.ok(Map.of("message", "Cuenta desbloqueada exitosamente"));
    }

    @Data
    static class ChangeEmailReq { private String newEmail; }
}
