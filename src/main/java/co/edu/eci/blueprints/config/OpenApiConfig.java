package co.edu.eci.blueprints.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("BluePrints API")
                        .version("2.0")
                        .description("""
                    ## BluePrints API – Parte 2: Seguridad con JWT (OAuth 2.0)

                    API REST protegida con **Spring Security OAuth2 Resource Server**.
                    Los tokens se firman con **RS256** y se validan localmente con la llave pública RSA.

                    ### Cómo autenticarse
                    1. Ejecutar `POST /auth/login` con usuario y contraseña.
                    2. Copiar el `access_token` de la respuesta.
                    3. Hacer clic en **Authorize** (arriba a la derecha).
                    4. Ingresar: `Bearer <token>` y confirmar.

                    ### Usuarios de prueba
                    | Usuario   | Contraseña    | Scopes                                  |
                    |-----------|---------------|-----------------------------------------|
                    | student   | student123    | blueprints.read, blueprints.write       |
                    | assistant | assistant123  | blueprints.read, blueprints.write       |

                    ### Scopes
                    | Scope              | Permisos                              |
                    |--------------------|---------------------------------------|
                    | blueprints.read    | GET /api/blueprints/**                |
                    | blueprints.write   | POST, PUT, DELETE /api/blueprints/**  |
                    """)
                        .contact(new Contact()
                                .name("Escuela Colombiana de Ingeniería Julio Garavito")
                                .url("https://www.escuelaing.edu.co"))
                )
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Servidor local de desarrollo"))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .name("bearer-jwt")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el token obtenido de POST /auth/login con el prefijo Bearer")));
    }
}