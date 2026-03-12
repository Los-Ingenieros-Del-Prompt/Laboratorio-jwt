# BluePrints API – Parte 2: Seguridad JWT (OAuth 2.0)

## Tecnologías

- Java 21
- Spring Boot 3.3
- Spring Security OAuth2 Resource Server
- JWT / RS256 (Nimbus JOSE)
- Swagger / OpenAPI 3 (SpringDoc)
- Maven 3.9+

---

## Cómo ejecutar

```bash
git clone https://github.com/Los-Ingenieros-Del-Prompt/Laboratorio-jwt.git
cd Laboratorio-jwt
mvn -q -DskipTests spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.

---

## Endpoints

### Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/auth/login` | Obtener token JWT | No |
| `GET` | `/auth/token-info` | Ver claims del token activo | Bearer |

### Blueprints

| Método | Endpoint | Descripción | Scope |
|--------|----------|-------------|-------|
| `GET` | `/api/blueprints` | Listar todos | `blueprints.read` |
| `GET` | `/api/blueprints/{id}` | Obtener por ID | `blueprints.read` |
| `GET` | `/api/blueprints/author/{author}` | Obtener por autor | `blueprints.read` |
| `POST` | `/api/blueprints` | Crear nuevo | `blueprints.write` |
| `PUT` | `/api/blueprints/{id}` | Actualizar | `blueprints.write` |
| `DELETE` | `/api/blueprints/{id}` | Eliminar | `blueprints.write` |

### Usuarios de prueba

| Usuario | Contraseña |
|---------|------------|
| `student` | `student123` |
| `assistant` | `assistant123` |

---

## Cómo autenticarse

**1. Obtener el token:**
```bash
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "student",
  "password": "student123"
}
```

**2. Usar el token en cada request:**
```bash
GET http://localhost:8080/api/blueprints
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

---

## Swagger UI

Disponible en [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)

1. Ejecutar `POST /auth/login` desde Swagger
2. Copiar el `access_token`
3. Clic en **Authorize** → ingresar `Bearer <token>`

---

## Estructura del proyecto

```
src/main/java/co/edu/eci/blueprints/
├── api/
│   └── BlueprintController.java      # Endpoints REST protegidos por scope
├── auth/
│   └── AuthController.java           # Login didáctico + /auth/token-info
├── config/
│   └── OpenApiConfig.java            # Configuración Swagger + JWT
└── security/
    ├── SecurityConfig.java           # Filtro HTTP, OAuth2 Resource Server
    ├── MethodSecurityConfig.java     # Habilita @PreAuthorize
    ├── JwtKeyProvider.java           # Generación del par RSA 2048-bit
    ├── InMemoryUserService.java      # Usuarios en memoria con BCrypt
    └── RsaKeyProperties.java        # Propiedades: issuer y TTL del token
```

---

## Configuración del token

En `src/main/resources/application.yml`:

```yaml
blueprints:
  security:
    issuer: "https://decsis-eci/blueprints"
    token-ttl-seconds: 3600  
```

