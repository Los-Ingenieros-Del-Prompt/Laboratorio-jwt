package co.edu.eci.blueprints.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Tag(name = "Blueprints", description = "Gestión de planos arquitectónicos")
@RestController
@RequestMapping("/api/blueprints")
public class BlueprintController {

    private final Map<String, Map<String, Object>> store = new ConcurrentHashMap<>();
    private final AtomicInteger idSeq = new AtomicInteger(1);

    public BlueprintController() {
        store.put("b1", blueprint("b1", "Casa de campo", "student"));
        store.put("b2", blueprint("b2", "Edificio urbano", "student"));
        store.put("b3", blueprint("b3", "Torre residencial", "assistant"));
    }

    private Map<String, Object> blueprint(String id, String name, String author) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("id", id);
        b.put("name", name);
        b.put("author", author);
        return b;
    }

    @Operation(
            summary = "Listar todos los blueprints",
            description = "Retorna la lista completa de planos. Requiere scope blueprints.read.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de blueprints"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Scope insuficiente")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public List<Map<String, Object>> list() {
        return new ArrayList<>(store.values());
    }


    @Operation(
            summary = "Obtener blueprint por ID",
            description = "Retorna el plano con el ID indicado. Requiere scope blueprints.read.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blueprint encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Scope insuficiente"),
            @ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        Map<String, Object> bp = store.get(id);
        if (bp == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bp);
    }

    @Operation(
            summary = "Listar blueprints por autor",
            description = "Retorna todos los planos pertenecientes a un autor. Requiere scope blueprints.read.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de blueprints del autor"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Scope insuficiente")
    })
    @GetMapping("/author/{author}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public List<Map<String, Object>> getByAuthor(@PathVariable String author) {
        return store.values().stream()
                .filter(b -> author.equals(b.get("author")))
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Crear un nuevo blueprint",
            description = "Crea un nuevo plano con el nombre indicado. Requiere scope blueprints.write.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blueprint creado"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Scope insuficiente")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public Map<String, Object> create(@RequestBody Map<String, String> in) {
        String newId = "b" + idSeq.getAndIncrement();
        String author = in.getOrDefault("author", "anonymous");
        Map<String, Object> bp = blueprint(newId, in.getOrDefault("name", "sin nombre"), author);
        store.put(newId, bp);
        return bp;
    }

    @Operation(
            summary = "Actualizar un blueprint",
            description = "Actualiza el nombre del plano indicado. Requiere scope blueprints.write.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blueprint actualizado"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Scope insuficiente"),
            @ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String id,
            @RequestBody Map<String, String> in) {
        Map<String, Object> bp = store.get(id);
        if (bp == null) {
            return ResponseEntity.notFound().build();
        }
        String currentName = (String) bp.get("name");
        bp.put("name", in.getOrDefault("name", currentName));
        return ResponseEntity.ok(bp);
    }


    @Operation(
            summary = "Eliminar un blueprint",
            description = "Elimina el plano con el ID indicado. Requiere scope blueprints.write.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Blueprint eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Scope insuficiente"),
            @ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!store.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        store.remove(id);
        return ResponseEntity.noContent().build();
    }
}