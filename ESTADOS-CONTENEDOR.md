# Estados del Sistema

## Estados de Contenedor

Los contenedores pueden tener los siguientes estados (siempre en **MAYÚSCULAS**):

### 1. **LIBRE**
- **Descripción**: Contenedor disponible para ser asignado a una solicitud
- **Cuándo se usa**: Estado inicial cuando se crea un contenedor
- **Transiciones permitidas**: → ASIGNADO

### 2. **ASIGNADO**
- **Descripción**: Contenedor asignado a una solicitud pero aún no en tránsito
- **Cuándo se usa**: Cuando se asigna un contenedor a una solicitud
- **Transiciones permitidas**: → EN_TRANSITO, → LIBRE (si se desasigna)

### 3. **EN_TRANSITO**
- **Descripción**: Contenedor siendo transportado activamente
- **Cuándo se usa**: Cuando inicia el transporte
- **Transiciones permitidas**: → EN_DEPOSITO, → ENTREGADO

### 4. **EN_DEPOSITO**
- **Descripción**: Contenedor almacenado temporalmente en un depósito intermedio
- **Cuándo se usa**: Cuando el contenedor se almacena en una parada intermedia
- **Transiciones permitidas**: → EN_TRANSITO (para continuar viaje), → ENTREGADO (si es el destino final)

### 5. **ENTREGADO**
- **Descripción**: Contenedor entregado en destino final
- **Cuándo se usa**: Cuando se completa la entrega
- **Transiciones permitidas**: → LIBRE (después de procesamiento/limpieza)

---

## Diagrama de Flujo de Estados

```
LIBRE
  ↓
ASIGNADO
  ↓
EN_TRANSITO ←→ EN_DEPOSITO
  ↓              ↓
ENTREGADO ← ─ ─ ─
  ↓
LIBRE (ciclo)
```

---

## Ejemplos de Uso en Postman

### Crear contenedor (estado inicial automático: LIBRE)
```json
{
  "id": null,
  "peso": 500.0,
  "volumen": 5.0,
  "clienteId": 10,
  "estado": "LIBRE"
}
```

### Cambiar estado del contenedor
```
PATCH {{baseUrl}}/api/v1/contenedores/{{contenedorId}}?estado=EN_TRANSITO
```

Valores válidos para el parámetro `estado`:
- `LIBRE`
- `ASIGNADO`
- `EN_TRANSITO`
- `EN_DEPOSITO`
- `ENTREGADO`

---

## Notas Importantes

⚠️ **Siempre usar MAYÚSCULAS**: Los estados deben escribirse en mayúsculas (ej: `LIBRE`, no `Libre` ni `libre`)

⚠️ **Estado DISPONIBLE deprecado**: Si encuentras referencias a `DISPONIBLE`, debe reemplazarse por `LIBRE`

⚠️ **Validación de transiciones**: El sistema puede validar que las transiciones de estado sean lógicas (por implementar según necesidades de negocio)
