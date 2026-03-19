# Escuela App — Backend

Spring Boot 3 + PostgreSQL + JWT

---

## Requisitos previos

| Herramienta  | Versión mínima |
|--------------|----------------|
| Java JDK     | 21             |
| Maven        | 3.9+           |
| PostgreSQL   | 15+            |

Verifica tu versión:
```bash
java -version
mvn -version
psql --version
```

---

## Instalación

### 1. Crear la base de datos en PostgreSQL

Conéctate a PostgreSQL con psql o pgAdmin y ejecuta:

```sql
CREATE DATABASE escuela_db;
```

Si quieres usar un usuario distinto a `postgres`:

```sql
CREATE USER escuela_user WITH PASSWORD 'tu_password';
GRANT ALL PRIVILEGES ON DATABASE escuela_db TO escuela_user;
```

### 2. Configurar `application.properties`

El archivo se encuentra en `src/main/resources/application.properties`. Edita los valores con los de tu entorno:

```properties
# Base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/escuela_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# JWT
app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
app.jwt.expiration-ms=86400000

# RF-15: verificación de identidad
app.reset-token.expiry-minutes=15
app.verification.max-attempts=5
app.verification.lockout-minutes=30

# URL del frontend para el enlace de reset
app.frontend.reset-url=http://localhost:5173/reset-password

# Correo (necesario para RF-12 y RF-15)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_correo@gmail.com
spring.mail.password=tu_app_password_gmail
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

server.port=8080
server.error.include-message=always
```

> **Nota Gmail:** necesitas una *App Password*, no tu contraseña normal.
> Ve a: Cuenta de Google → Seguridad → Verificación en 2 pasos → Contraseñas de aplicaciones.

### 3. Instalar dependencias con Maven

```bash
mvn dependency:resolve
```

O directamente al compilar (descarga todo automáticamente):

```bash
mvn clean package -DskipTests
```

---

## Correr el proyecto

### Modo desarrollo

```bash
mvn spring-boot:run
```

### Compilar y ejecutar JAR

```bash
mvn clean package -DskipTests
java -jar target/*.jar
```

El backend queda disponible en: `http://localhost:8080`

---

## Estructura del proyecto

```
src/main/java/com/pruebas/
├── PruebasApplication.java
├── domain/
│   ├── model/
│   │   └── User.java                     — modelo de dominio
│   └── ports/
│       └── UserRepositoryPort.java       — interfaz de repositorio
├── application/
│   └── services/
│       ├── AuthService.java              — RF-1, RF-2, RF-12, RF-14, RF-15
│       ├── AccountService.java           — cambio de correo, sesiones, desbloqueo
│       └── AuditService.java             — auditoría persistente en BD
└── infrastructure/
    ├── config/
    │   ├── SecurityConfig.java           — Spring Security + JWT
    │   ├── JwtService.java               — generación y validación de tokens
    │   └── JwtAuthFilter.java            — filtro de autenticación
    ├── controllers/
    │   ├── AuthController.java           — endpoints de autenticación
    │   ├── AccountController.java        — endpoints de gestión de cuenta
    │   └── GlobalExceptionHandler.java   — manejo global de errores HTTP
    ├── entity/
    │   ├── UserEntity.java               — entidad JPA usuarios
    │   └── AuditLogEntity.java           — entidad JPA auditoría
    ├── repository/
    │   ├── JpaUserRepository.java
    │   └── AuditLogRepository.java
    └── adapters/
        └── JpaUserRepositoryAdapter.java — implementación del puerto de dominio
```

---

## Endpoints disponibles

### Autenticación pública

| Método | Endpoint                          | Descripción                  |
|--------|-----------------------------------|------------------------------|
| POST   | `/api/auth/login`                 | Iniciar sesión (RF-1)        |
| POST   | `/api/auth/register`              | Registrar usuario (RF-2)     |
| POST   | `/api/auth/forgot-password?email=`| Solicitar reset (RF-12)      |
| POST   | `/api/auth/reset-password`        | Aplicar nueva contraseña     |

### Verificación de identidad (requieren JWT)

| Método | Endpoint                           | Descripción                       |
|--------|------------------------------------|-----------------------------------|
| POST   | `/api/auth/verify/send-code`       | Enviar código temporal (RF-15)    |
| POST   | `/api/auth/verify/validate-code`   | Validar código                    |
| POST   | `/api/auth/verify/change-password` | Cambiar contraseña verificada     |

### Gestión de cuenta (requieren JWT)

| Método | Endpoint                      | Descripción                  |
|--------|-------------------------------|------------------------------|
| PUT    | `/api/account/email`          | Cambiar correo electrónico   |
| POST   | `/api/account/close-sessions` | Cerrar sesiones activas      |
| POST   | `/api/account/unlock`         | Desbloquear cuenta           |

---

## Reglas de negocio

| Regla | Descripción                                                        |
|-------|--------------------------------------------------------------------|
| RB1   | Token y código expiran en 15 min (configurable en properties)      |
| RB2   | Token y código son de un solo uso                                  |
| RB3   | Máx 5 intentos fallidos → bloqueo 30 min (configurable)           |
| RB4   | Contraseña: mín 12 chars, mayúscula, minúscula, número y símbolo  |
| RB5   | Al cambiar contraseña se emite nuevo JWT                           |
| RB6   | Auditoría: cada evento sensible persiste en tabla `audit_log`      |

---

## Dependencias principales (`pom.xml`)

```xml
<!-- Spring Boot -->
<dependency>spring-boot-starter-web</dependency>
<dependency>spring-boot-starter-security</dependency>
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>spring-boot-starter-mail</dependency>

<!-- Base de datos -->
<dependency>postgresql</dependency>

<!-- JWT -->
<dependency>jjwt-api</dependency>
<dependency>jjwt-impl</dependency>
<dependency>jjwt-jackson</dependency>

<!-- Utilidades -->
<dependency>lombok</dependency>
```

---

## Despliegue con Docker

Desde la raíz del proyecto:

```bash
docker-compose up --build
```

Levanta PostgreSQL, backend y frontend en red interna. Ver `docker-compose.yml` para la configuración completa.

Para correr en segundo plano:

```bash
docker-compose up --build -d
```

Para ver logs:

```bash
docker-compose logs -f backend
```
