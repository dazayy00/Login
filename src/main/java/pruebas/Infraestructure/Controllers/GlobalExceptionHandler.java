package pruebas.Infraestructure.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<String, HttpStatus> STATUS_MAP = Map.of(
            "credenciales incorrectas",        HttpStatus.UNAUTHORIZED,
            "cuenta está inactiva",            HttpStatus.FORBIDDEN,
            "cuenta bloqueada",                HttpStatus.TOO_MANY_REQUESTS,
            "múltiples intentos",              HttpStatus.TOO_MANY_REQUESTS,
            "código ha expirado",              HttpStatus.GONE,
            "enlace ha expirado",              HttpStatus.GONE,
            "enlace no es válido",             HttpStatus.BAD_REQUEST,
            "código ya fue utilizado",         HttpStatus.BAD_REQUEST,
            "no hay código",                   HttpStatus.BAD_REQUEST,
            "contraseña actual es incorrecta", HttpStatus.UNAUTHORIZED
    );

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,Object>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Error interno";
        HttpStatus status = STATUS_MAP.entrySet().stream()
                .filter(e -> msg.toLowerCase().contains(e.getKey().toLowerCase()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(status).body(Map.of(
                "message", msg, "status", status.value(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(500).body(Map.of(
                "message", "Error interno del servidor",
                "status", 500,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
