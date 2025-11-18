# Mejoras: Solicitudes con Depósitos

## 🔍 Problema Actual

Las **Solicitudes** actualmente usan direcciones de texto libre:
```java
private String direccionOrigen;    // Texto libre: "Buenos Aires", "Rosario", etc.
private String direccionDestino;   // Texto libre
```

Pero ahora el **CalculoService** solo acepta:
1. IDs de depósito (ej: "1", "2", "3")
2. Coordenadas directas (ej: "-34.6037,-58.3816")

**Resultado**: Las solicitudes no pueden calcular distancias correctamente porque están enviando texto libre que el servicio rechaza.

## 💡 Solución Propuesta

### Opción 1: Agregar Depósitos a Solicitudes ⭐ (Recomendada)

Modificar el modelo `Solicitud` para incluir referencias a depósitos:

```java
@Entity
@Table(name = "solicitudes")
public class Solicitud {
    // ... campos existentes ...
    
    // NUEVOS CAMPOS
    @Column(name = "origen_deposito_id")
    private Long origenDepositoId;
    
    @Column(name = "destino_deposito_id")
    private Long destinoDepositoId;
    
    // Mantener campos legacy para compatibilidad
    @Column(name = "direccion_origen")
    private String direccionOrigen;
    
    @Column(name = "direccion_destino")
    private String direccionDestino;
}
```

**Lógica de cálculo mejorada**:
```java
// En SolicitudService.calculatePrice()
String origen, destino;

// Priorizar depósitos sobre direcciones
if (solicitud.getOrigenDepositoId() != null) {
    origen = String.valueOf(solicitud.getOrigenDepositoId());
} else if (solicitud.getOrigenLat() != null && solicitud.getOrigenLong() != null) {
    origen = solicitud.getOrigenLat() + "," + solicitud.getOrigenLong();
} else {
    origen = solicitud.getDireccionOrigen(); // Fallback legacy
}

// Similar para destino...

distanciaReq.put("origen", origen);
distanciaReq.put("destino", destino);
```

**Ventajas**:
- ✅ Integración completa con sistema de depósitos
- ✅ Cálculo de distancias preciso con OSRM
- ✅ Compatibilidad con rutas automáticas
- ✅ Mantiene compatibilidad con datos existentes

**Desventajas**:
- ⚠️ Requiere migración de base de datos
- ⚠️ Requiere actualizar DTOs y endpoints

---

### Opción 2: Flexibilizar CalculoService (NO Recomendada)

Volver a permitir texto libre en `CalculoService.geocodificar()`:

```java
// Re-agregar el mapa de ciudades
private static final Map<String, CoordenadaDTO> CIUDADES = Map.of(
    "Buenos Aires", new CoordenadaDTO(-34.6037, -58.3816),
    "Rosario", new CoordenadaDTO(-32.9445, -60.6500),
    // ...
);
```

**Ventajas**:
- ✅ No requiere cambios en solicitudes
- ✅ Funciona con datos existentes

**Desventajas**:
- ❌ Cálculos imprecisos (ciudad → ciudad en vez de depósito → depósito)
- ❌ No aprovecha sistema de depósitos
- ❌ No permite rutas automáticas
- ❌ Volvemos al problema original

---

### Opción 3: Solución Híbrida ⚠️ (Temporal)

Permitir **ambos** en `CalculoService`:
1. Primero intentar ID de depósito
2. Luego intentar coordenadas
3. Finalmente intentar texto (ciudades conocidas)

```java
private CoordenadaDTO geocodificar(String direccion) {
    // 1. ¿Es ID de depósito?
    if (esNumeroEntero(direccion)) {
        return consultarCoordenadasDeposito(Long.parseLong(direccion));
    }
    
    // 2. ¿Son coordenadas?
    if (direccion.contains(",")) {
        return parsearCoordenadas(direccion);
    }
    
    // 3. ¿Es ciudad conocida? (LEGACY - deprecado)
    if (CIUDADES.containsKey(direccion)) {
        logger.warn("Usando geocodificación legacy para ciudad: {}", direccion);
        return CIUDADES.get(direccion);
    }
    
    throw new IllegalArgumentException("Formato inválido...");
}
```

**Ventajas**:
- ✅ Compatibilidad con solicitudes existentes
- ✅ Permite migración gradual

**Desventajas**:
- ⚠️ Complejidad adicional
- ⚠️ Cálculos imprecisos para solicitudes sin depósitos
- ⚠️ Código legacy que eventualmente hay que eliminar

---

## 📋 Implementación Recomendada (Opción 1)

### 1. Modificar Modelo `Solicitud`

```java
// Agregar campos:
private Long origenDepositoId;
private Long destinoDepositoId;
```

### 2. Migración SQL

```sql
-- Agregar columnas a solicitudes
ALTER TABLE solicitudes ADD COLUMN IF NOT EXISTS origen_deposito_id BIGINT;
ALTER TABLE solicitudes ADD COLUMN IF NOT EXISTS destino_deposito_id BIGINT;

-- Agregar foreign keys (opcional)
ALTER TABLE solicitudes 
    ADD CONSTRAINT fk_solicitudes_origen_deposito 
    FOREIGN KEY (origen_deposito_id) 
    REFERENCES depositos(id_deposito);

ALTER TABLE solicitudes 
    ADD CONSTRAINT fk_solicitudes_destino_deposito 
    FOREIGN KEY (destino_deposito_id) 
    REFERENCES depositos(id_deposito);

-- Crear índices
CREATE INDEX IF NOT EXISTS idx_solicitudes_origen_deposito ON solicitudes(origen_deposito_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_destino_deposito ON solicitudes(destino_deposito_id);
```

### 3. Actualizar DTOs

```java
// CreateSolicitudDTO
@Data
public class CreateSolicitudDTO {
    private Long contenedorId;
    private Long clienteId;
    
    // NUEVOS - Preferir depósitos
    private Long origenDepositoId;
    private Long destinoDepositoId;
    
    // OPCIONALES - Solo si no se usan depósitos
    private String direccionOrigen;
    private String direccionDestino;
    private BigDecimal origenLat;
    private BigDecimal origenLong;
    private BigDecimal destinoLat;
    private BigDecimal destinoLong;
}

// SolicitudDTO
@Data
public class SolicitudDTO {
    // ... campos existentes ...
    private Long origenDepositoId;
    private Long destinoDepositoId;
    private String origenDepositoNombre;   // Nuevo - para mostrar
    private String destinoDepositoNombre;  // Nuevo - para mostrar
}
```

### 4. Modificar `SolicitudService.calculatePrice()`

```java
public Object calculatePrice(Long solicitudId) {
    Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud not found: " + solicitudId));
    
    // Construir request con prioridad: depósito > coordenadas > dirección
    String origen = determinarOrigen(solicitud);
    String destino = determinarDestino(solicitud);
    
    Map<String, String> distanciaReq = new HashMap<>();
    distanciaReq.put("origen", origen);
    distanciaReq.put("destino", destino);
    
    // ... resto del método ...
}

private String determinarOrigen(Solicitud solicitud) {
    // 1. Prioridad: Depósito
    if (solicitud.getOrigenDepositoId() != null) {
        return String.valueOf(solicitud.getOrigenDepositoId());
    }
    
    // 2. Coordenadas directas
    if (solicitud.getOrigenLat() != null && solicitud.getOrigenLong() != null) {
        return solicitud.getOrigenLat() + "," + solicitud.getOrigenLong();
    }
    
    // 3. Dirección de texto (legacy - deprecado)
    if (solicitud.getDireccionOrigen() != null) {
        logger.warn("Usando dirección de texto legacy para solicitud {}: {}", 
                solicitud.getId(), solicitud.getDireccionOrigen());
        return solicitud.getDireccionOrigen();
    }
    
    throw new IllegalArgumentException("No se pudo determinar origen para solicitud " + solicitud.getId());
}
```

### 5. Enriquecer DTOs con Nombres de Depósitos

```java
// En SolicitudService.toDto()
private SolicitudDTO toDto(Solicitud solicitud) {
    SolicitudDTO dto = new SolicitudDTO();
    // ... mapeo existente ...
    
    dto.setOrigenDepositoId(solicitud.getOrigenDepositoId());
    dto.setDestinoDepositoId(solicitud.getDestinoDepositoId());
    
    // Consultar nombres de depósitos si existen
    if (solicitud.getOrigenDepositoId() != null) {
        dto.setOrigenDepositoNombre(obtenerNombreDeposito(solicitud.getOrigenDepositoId()));
    }
    if (solicitud.getDestinoDepositoId() != null) {
        dto.setDestinoDepositoNombre(obtenerNombreDeposito(solicitud.getDestinoDepositoId()));
    }
    
    return dto;
}

private String obtenerNombreDeposito(Long depositoId) {
    try {
        // Llamar a ms-gestion-calculos para obtener nombre
        String token = extractBearerToken();
        ResponseEntity<Map<String, Object>> response = calculosClient.get()
                .uri("/api/v1/depositos/{id}/coordenadas", depositoId)
                .headers(h -> { if (token != null) h.setBearerAuth(token); })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
        
        if (response.getBody() != null) {
            return (String) response.getBody().get("nombre");
        }
    } catch (Exception e) {
        logger.warn("No se pudo obtener nombre del depósito {}", depositoId);
    }
    return null;
}
```

### 6. Actualizar Validaciones

```java
// En SolicitudService.create()
public SolicitudDTO create(CreateSolicitudDTO dto) {
    // Validar que haya al menos un método de origen
    if (dto.getOrigenDepositoId() == null && 
        dto.getDireccionOrigen() == null && 
        (dto.getOrigenLat() == null || dto.getOrigenLong() == null)) {
        throw new IllegalArgumentException(
            "Debe especificar origen mediante: depósito, dirección, o coordenadas");
    }
    
    // Validar que haya al menos un método de destino
    if (dto.getDestinoDepositoId() == null && 
        dto.getDireccionDestino() == null && 
        (dto.getDestinoLat() == null || dto.getDestinoLong() == null)) {
        throw new IllegalArgumentException(
            "Debe especificar destino mediante: depósito, dirección, o coordenadas");
    }
    
    // ... resto de la validación ...
}
```

---

## 🔄 Migración de Datos Existentes

### Opción A: Mantener datos existentes sin conversión
```sql
-- No hacer nada - dejar origen_deposito_id y destino_deposito_id en NULL
-- El sistema seguirá usando direccion_origen y direccion_destino
```
**Ventaja**: Sin riesgo de pérdida de datos  
**Desventaja**: Solicitudes antiguas tendrán cálculos menos precisos

### Opción B: Convertir direcciones a depósitos (manual)
```sql
-- Ejemplo: Convertir "Buenos Aires" → Depósito Central (ID 1)
UPDATE solicitudes 
SET origen_deposito_id = 1 
WHERE direccion_origen ILIKE '%buenos aires%' 
  AND origen_deposito_id IS NULL;
```
**Ventaja**: Mejora precisión de datos históricos  
**Desventaja**: Requiere mapeo manual ciudad → depósito

### Opción C: No migrar, marcar como legacy
```sql
-- Agregar flag para solicitudes legacy
ALTER TABLE solicitudes ADD COLUMN es_legacy BOOLEAN DEFAULT FALSE;

UPDATE solicitudes 
SET es_legacy = TRUE 
WHERE origen_deposito_id IS NULL 
  AND destino_deposito_id IS NULL;
```

---

## 🚀 Plan de Implementación

### Fase 1: Backend (Modelos y Servicios)
1. ✅ Agregar campos `origenDepositoId`, `destinoDepositoId` a modelo `Solicitud`
2. ✅ Actualizar DTOs (`CreateSolicitudDTO`, `SolicitudDTO`)
3. ✅ Modificar `SolicitudService.calculatePrice()` con lógica de prioridad
4. ✅ Agregar método `determinarOrigen()` y `determinarDestino()`
5. ✅ Enriquecer DTOs con nombres de depósitos

### Fase 2: Base de Datos
1. ✅ Ejecutar script de migración SQL
2. ⏳ Decidir estrategia de migración de datos existentes

### Fase 3: Testing
1. ⏳ Probar creación de solicitud con depósitos
2. ⏳ Probar cálculo de precio con depósitos
3. ⏳ Probar compatibilidad con solicitudes legacy (sin depósitos)

### Fase 4: Documentación
1. ⏳ Actualizar Postman con ejemplos de depósitos
2. ⏳ Documentar endpoints actualizados
3. ⏳ Crear guía de migración para usuarios

---

## 📝 Resumen

**Problema**: Solicitudes usan direcciones de texto libre que ya no son compatibles con el sistema de cálculo de distancias basado en depósitos.

**Solución**: Agregar campos `origenDepositoId` y `destinoDepositoId` a las solicitudes, manteniendo compatibilidad con datos legacy.

**Impacto**: 
- ✅ Cálculos de distancia más precisos
- ✅ Integración completa con sistema de depósitos
- ✅ Facilita creación automática de rutas
- ✅ Compatibilidad con datos existentes

**Próximo paso**: Implementar Fase 1 (Backend).
