# Rutas con Tramos Automáticos

## Resumen
El sistema ahora calcula automáticamente los tramos de una ruta cuando se crea especificando depósitos origen, destino y opcionalmente depósitos intermedios. Los tramos se generan utilizando cálculos reales de distancia y duración mediante OSRM.

## Creación de Rutas con Tramos Automáticos

### Endpoint
```
POST /api/v1/rutas
```

### DTO de Creación (CreateRutaDTO)

```java
{
  "idSolicitud": 1,                      // Obligatorio
  "origenDepositoId": 1,                 // Opcional - ID del depósito origen
  "destinoDepositoId": 3,                // Opcional - ID del depósito destino
  "depositosIntermediosIds": [2],        // Opcional - IDs de depósitos intermedios en orden
  "calcularRutaOptima": true             // Opcional - Si true, calcula múltiples variantes y elige la más corta
}
```

### Flujo de Creación

#### 1. Ruta sin Tramos Automáticos (comportamiento anterior)
```json
{
  "idSolicitud": 1
}
```
- Se crea la ruta vacía
- No se generan tramos automáticamente
- Los tramos deben agregarse manualmente después

#### 2. Ruta con Depósitos Específicos
```json
{
  "idSolicitud": 1,
  "origenDepositoId": 1,
  "destinoDepositoId": 3,
  "depositosIntermediosIds": [2]
}
```
- Se crea la ruta
- Se calcula la ruta exacta: Depósito 1 → Depósito 2 → Depósito 3
- Se generan 2 tramos automáticamente:
  - Tramo 1: Depósito 1 → Depósito 2
  - Tramo 2: Depósito 2 → Depósito 3

#### 3. Ruta Óptima (el sistema elige la mejor ruta)
```json
{
  "idSolicitud": 1,
  "origenDepositoId": 1,
  "destinoDepositoId": 3,
  "calcularRutaOptima": true
}
```
- Se crea la ruta
- El sistema calcula múltiples variantes:
  - Ruta directa: Depósito 1 → Depósito 3
  - Rutas con intermedios: Depósito 1 → Depósito X → Depósito 3 (para cada depósito X disponible)
- Se elige automáticamente la ruta más corta
- Se generan los tramos correspondientes a la ruta elegida

### Campos de Tramo Generados

Cuando se crea un tramo automáticamente, se incluye:

```java
{
  "id": 1,
  "idRuta": 1,
  "orden": 1,                           // Orden del tramo en la ruta (1, 2, 3...)
  "origenDepositoId": 1,
  "destinoDepositoId": 2,
  "distancia": 298.5,                   // Distancia real en km (calculada con OSRM)
  "duracionHoras": 3.2,                 // Duración estimada en horas (calculada con OSRM)
  "generadoAutomaticamente": true,      // Marca que este tramo fue generado automáticamente
  "camionDominio": null,                // Sin asignar inicialmente
  "estado": null                        // Estado pendiente
}
```

## Problema: Agregar Tramos Manualmente

### Escenario Problemático

1. **Se crea una ruta con tramos automáticos**
   ```
   Ruta ID 1: Buenos Aires (Dep 1) → Córdoba (Dep 2) → Rosario (Dep 3)
   Tramos generados:
   - Tramo 1 (orden 1): Dep 1 → Dep 2 | 700 km | generadoAutomaticamente=true
   - Tramo 2 (orden 2): Dep 2 → Dep 3 | 400 km | generadoAutomaticamente=true
   ```

2. **El usuario agrega un tramo manual**
   ```
   Nuevo tramo: Mendoza (Dep 4) → Rosario (Dep 3)
   - Tramo 3 (orden 3): Dep 4 → Dep 3 | 600 km | generadoAutomaticamente=false
   ```

3. **Problema: La ruta ya no tiene sentido lógico**
   ```
   Ruta resultante:
   - Tramo 1: Dep 1 → Dep 2 (Buenos Aires → Córdoba)
   - Tramo 2: Dep 2 → Dep 3 (Córdoba → Rosario)
   - Tramo 3: Dep 4 → Dep 3 (Mendoza → Rosario) ❌ No conecta con el tramo anterior!
   ```

### Soluciones Propuestas

#### Opción 1: Protección Estricta (Recomendada)
**Prohibir agregar tramos manuales si existen tramos automáticos**

```java
// En TramoService.create()
if (tramoRepository.existsByRutaIdAndGeneradoAutomaticamente(rutaId, true)) {
    throw new IllegalArgumentException(
        "No se pueden agregar tramos manuales a una ruta con tramos automáticos. " +
        "Elimine los tramos automáticos primero o recalcule la ruta completa."
    );
}
```

**Ventajas:**
- Mantiene la integridad de la ruta
- Evita rutas incoherentes
- Fuerza al usuario a recalcular si quiere cambios

**Desventajas:**
- Menos flexible
- El usuario debe eliminar todos los tramos automáticos para hacer cambios manuales

#### Opción 2: Validación de Continuidad
**Validar que los tramos formen una cadena continua**

```java
// Validar que el nuevo tramo conecta con el último tramo existente
List<Tramo> tramosExistentes = tramoRepository.findByRutaIdOrderByOrdenAsc(rutaId);
if (!tramosExistentes.isEmpty()) {
    Tramo ultimoTramo = tramosExistentes.get(tramosExistentes.size() - 1);
    if (!nuevoTramo.getOrigenDepositoId().equals(ultimoTramo.getDestinoDepositoId())) {
        throw new IllegalArgumentException(
            "El nuevo tramo debe comenzar donde termina el último tramo. " +
            "El último tramo termina en depósito " + ultimoTramo.getDestinoDepositoId()
        );
    }
}
```

**Ventajas:**
- Más flexible
- Permite extender rutas automáticas

**Desventajas:**
- Solo valida orden secuencial, no valida si tiene sentido geográfico
- Permite romper la optimización de la ruta original

#### Opción 3: Modo Híbrido
**Permitir agregar tramos pero marcar la ruta como "modificada manualmente"**

```java
// Agregar campo en Ruta
private Boolean modificadaManualmente;

// Al agregar tramo manual
if (tramoRepository.existsByRutaIdAndGeneradoAutomaticamente(rutaId, true)) {
    ruta.setModificadaManualmente(true);
    rutaRepository.save(ruta);
    logger.warn("Ruta {} marcada como modificada manualmente - la optimización ya no es válida", rutaId);
}
```

**Ventajas:**
- Máxima flexibilidad
- Mantiene trazabilidad de cambios

**Desventajas:**
- Permite rutas potencialmente incoherentes
- Requiere validación adicional en otros puntos

#### Opción 4: Recálculo Automático
**Al agregar/modificar tramos, recalcular toda la ruta**

```java
// Al agregar un tramo manual
if (tramosAutomaticos.exists()) {
    // Eliminar todos los tramos automáticos
    tramoRepository.deleteByRutaIdAndGeneradoAutomaticamente(rutaId, true);
    
    // Recalcular ruta completa incluyendo el nuevo tramo
    List<Long> depositosIds = extraerDepositosDeTramos(todosLosTramos);
    RutaTentativaDTO nuevaRuta = rutaTentativaService.calcularMejorRuta(...);
    crearTramosDesdeRutaTentativa(nuevaRuta);
}
```

**Ventajas:**
- Siempre mantiene rutas optimizadas
- Automático y transparente

**Desventajas:**
- Puede modificar tramos que el usuario no quería cambiar
- Costoso computacionalmente

## Recomendación de Implementación

**Implementar Opción 1 (Protección Estricta) + Opción 3 (Modo Híbrido)**

### Flujo propuesto:

1. **Al crear ruta con depósitos**: Se generan tramos automáticos con `generadoAutomaticamente=true`

2. **Al intentar agregar tramo manual**:
   ```
   Si existen tramos automáticos:
     Mostrar advertencia: "Esta ruta tiene tramos generados automáticamente. 
                          ¿Desea eliminarlos y trabajar en modo manual?"
     
     Opción A: "Sí, trabajar en modo manual"
       → Eliminar todos los tramos automáticos
       → Crear el nuevo tramo manual
       → Marcar ruta como modificadaManualmente=true
     
     Opción B: "No, recalcular ruta completa"
       → Extraer todos los depósitos (automáticos + nuevo)
       → Recalcular ruta óptima
       → Reemplazar tramos
   ```

3. **Al modificar/eliminar tramos**:
   - Si es tramo automático: Marcar ruta como modificadaManualmente=true
   - Validar que no se rompa la continuidad (destino tramo N = origen tramo N+1)

### Código de ejemplo:

```java
@Transactional
public TramoDTO agregarTramoManual(Long rutaId, CreateTramoDTO nuevoTramo, boolean eliminarAutomaticos) {
    // Verificar si hay tramos automáticos
    boolean hayAutomaticos = tramoRepository.existsByRutaIdAndGeneradoAutomaticamente(rutaId, true);
    
    if (hayAutomaticos && !eliminarAutomaticos) {
        throw new IllegalArgumentException(
            "Esta ruta tiene tramos generados automáticamente. " +
            "Use eliminarAutomaticos=true para trabajar en modo manual."
        );
    }
    
    if (eliminarAutomaticos) {
        // Eliminar todos los tramos automáticos
        tramoRepository.deleteByRutaIdAndGeneradoAutomaticamente(rutaId, true);
        logger.info("Tramos automáticos eliminados de ruta {}", rutaId);
        
        // Marcar ruta como modificada manualmente
        Ruta ruta = rutaRepository.findById(rutaId).orElseThrow();
        ruta.setModificadaManualmente(true);
        rutaRepository.save(ruta);
    }
    
    // Crear el nuevo tramo manual
    Tramo tramo = new Tramo();
    // ... mapear campos ...
    tramo.setGeneradoAutomaticamente(false);
    
    return toDto(tramoRepository.save(tramo));
}
```

## Ejemplos de Uso

### Caso 1: Ruta Completamente Automática
```bash
# Crear ruta con optimización automática
POST /api/v1/rutas
{
  "idSolicitud": 1,
  "origenDepositoId": 1,
  "destinoDepositoId": 5,
  "calcularRutaOptima": true
}

# Resultado: Ruta óptima con 3 tramos automáticos
# Tramo 1: Dep 1 → Dep 2 (350 km)
# Tramo 2: Dep 2 → Dep 4 (280 km)
# Tramo 3: Dep 4 → Dep 5 (190 km)
# Total: 820 km
```

### Caso 2: Extender Ruta Automática (Modo Manual)
```bash
# Agregar tramo manual eliminando automáticos
POST /api/v1/tramos?eliminarAutomaticos=true
{
  "rutaId": 1,
  "origenDepositoId": 5,
  "destinoDepositoId": 6
}

# Resultado: 
# - Se eliminaron los 3 tramos automáticos
# - Se creó 1 tramo manual: Dep 5 → Dep 6
# - Ruta marcada como modificadaManualmente=true
# - Usuario debe agregar manualmente los tramos anteriores si los necesita
```

### Caso 3: Recalcular Ruta Completa
```bash
# Recalcular ruta incluyendo nuevo depósito
PUT /api/v1/rutas/1/recalcular
{
  "origenDepositoId": 1,
  "destinoDepositoId": 6,
  "calcularRutaOptima": true
}

# Resultado: Nueva ruta óptima con todos los depósitos
# Se eliminan tramos anteriores y se generan nuevos optimizados
```

## Campos de Base de Datos

### Modificaciones necesarias en tabla `tramos`:

```sql
ALTER TABLE tramos ADD COLUMN orden INTEGER;
ALTER TABLE tramos ADD COLUMN generado_automaticamente BOOLEAN DEFAULT FALSE;
ALTER TABLE tramos ADD COLUMN duracion_horas DOUBLE PRECISION;
```

### Modificaciones necesarias en tabla `rutas`:

```sql
ALTER TABLE rutas ADD COLUMN modificada_manualmente BOOLEAN DEFAULT FALSE;
```

## Próximos Pasos

1. ✅ Agregar campos `orden`, `generadoAutomaticamente`, `duracionHoras` al modelo `Tramo`
2. ✅ Modificar `RutaService.create()` para generar tramos automáticos
3. ✅ Actualizar `TramoDTO` y `toDto()` para incluir nuevos campos
4. ⏳ Agregar validaciones en `TramoService.create()` para proteger tramos automáticos
5. ⏳ Agregar campo `modificadaManualmente` al modelo `Ruta`
6. ⏳ Crear endpoint `POST /api/v1/tramos` con parámetro `eliminarAutomaticos`
7. ⏳ Crear endpoint `PUT /api/v1/rutas/{id}/recalcular`
8. ⏳ Ejecutar migraciones SQL en PostgreSQL
9. ⏳ Actualizar tests
10. ⏳ Documentar en Postman
