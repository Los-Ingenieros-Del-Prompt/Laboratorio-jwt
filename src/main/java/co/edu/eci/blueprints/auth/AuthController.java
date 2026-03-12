package co.edu.eci.blueprints.auth;

import co.edu.eci.blueprints.security.InMemoryUserService;
import co.edu.eci.blueprints.security.RsaKeyProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@Tag(name = "Autenticación", description = "Emisión y consulta de tokens JWT (flujo didáctico)")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtEncoder encoder;
    private final InMemoryUserService userService;
    private final RsaKeyProperties props;

    public AuthController(JwtEncoder encoder, InMemoryUserService userService, RsaKeyProperties props) {
        this.encoder = encoder;
        this.userService = userService;
        this.props = props;
    }


    @Schema(description = "Credenciales de acceso")
    public record LoginRequest(
            @Schema(description = "Nombre de usuario", example = "student") String username,
            @Schema(description = "Contraseña", example = "student123") String password
    ) {}

    @Schema(description = "Token de acceso emitido")
    public record TokenResponse(
            @Schema(description = "JWT firmado en RS256") String access_token,
            @Schema(description = "Tipo de token", example = "Bearer") String token_type,
            @Schema(description = "Tiempo de vida en segundos", example = "3600") long expires_in
    ) {}



    @Operation(
            summary = "Iniciar sesión y obtener token",
            description = """
            Valida las credenciales y emite un JWT firmado con RS256.
            El token incluye las claims: iss, sub, iat, exp y scope.

            **Usuarios de prueba disponibles:**
            | Usuario    | Contraseña    |
            |------------|---------------|
            | student    | student123    |
            | assistant  | assistant123  |

            > **Actividad 4:** cambia `token-ttl-seconds` en `application.yml`
            > y repite el login para observar el cambio en la claim `exp`.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso – retorna el token Bearer",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "access_token": "eyJhbGciOiJSUzI1NiJ9...",
                      "token_type": "Bearer",
                      "expires_in": 3600
                    }
                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"invalid_credentials\"}")
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (!userService.isValid(req.username(), req.password())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }

        Instant now = Instant.now();
        long ttl = props.effectiveTtl();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttl))
                .subject(req.username())
                .claim("scope", "blueprints.read blueprints.write")
                .build();

        JwsHeader jws = JwsHeader.with(() -> "RS256").build();
        String token = encoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();

        return ResponseEntity.ok(new TokenResponse(token, "Bearer", ttl));
    }



    @Operation(
            summary = "Inspeccionar claims del token actual",
            description = """
            Retorna las claims del JWT presentado en el header Authorization.
            Útil para la **Actividad 2** (analizar claims) y la **Actividad 4**
            (verificar el TTL configurado sin necesidad de copiar el token a jwt.io).
            """,
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Claims del token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                      "subject": "student",
                      "issuer": "https://decsis-eci/blueprints",
                      "issuedAt": "2025-01-01T00:00:00Z",
                      "expiresAt": "2025-01-01T01:00:00Z",
                      "scope": "blueprints.read blueprints.write",
                      "ttl_seconds": 3600,
                      "ttl_minutes": 60.0
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> tokenInfo(@AuthenticationPrincipal Jwt jwt) {
        Instant iat = jwt.getIssuedAt();
        Instant exp = jwt.getExpiresAt();
        long ttlSeconds = (iat != null && exp != null) ? exp.getEpochSecond() - iat.getEpochSecond() : -1;

        return ResponseEntity.ok(Map.of(
                "subject",    jwt.getSubject(),
                "issuer",     jwt.getIssuer() != null ? jwt.getIssuer().toString() : "N/A",
                "issuedAt",   iat != null ? iat.toString() : "N/A",
                "expiresAt",  exp != null ? exp.toString() : "N/A",
                "scope",      jwt.getClaimAsString("scope"),
                "ttl_seconds", ttlSeconds,
                "ttl_minutes", ttlSeconds / 60.0
        ));
    }
}