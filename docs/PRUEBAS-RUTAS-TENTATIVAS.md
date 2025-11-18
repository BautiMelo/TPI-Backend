# Script de Prueba - Rutas Tentativas con Depósitos

## 📋 Prerequisitos

1. Sistema levantado con `docker-compose up`
2. Token de autenticación válido
3. Al menos 2 depósitos creados con coordenadas

## 🧪 Pruebas Paso a Paso

### 1. Configurar Variables

```bash
# Token de autenticación (reemplazar con token real)
export TOKEN="eyJhbGc..."

# URL base del API Gateway
export API_URL="http://localhost:8080"
```

### 2. Verificar/Crear Depósitos

```bash
# Listar depósitos existentes
curl -X GET "$API_URL/api/v1/depositos" \
  -H "Authorization: Bearer $TOKEN"

# Crear depósito Buenos Aires (si no existe)
curl -X POST "$API_URL/api/v1/depositos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Depósito Central Buenos Aires",
    "direccion": "Av. Córdoba 1234, CABA",
    "latitud": -34.6037,
    "longitud": -58.3816,
    "idCiudad": 1
  }'

# Crear depósito Rosario
curl -X POST "$API_URL/api/v1/depositos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Depósito Rosario Norte",
    "direccion": "Av. Pellegrini 5678, Rosario",
    "latitud": -32.9445,
    "longitud": -60.6500,
    "idCiudad": 2
  }'

# Crear depósito Córdoba
curl -X POST "$API_URL/api/v1/depositos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Depósito Córdoba Centro",
    "direccion": "Av. Colón 9012, Córdoba",
    "latitud": -31.4201,
    "longitud": -64.1888,
    "idCiudad": 3
  }'
```

### 3. Consultar Coordenadas de Depósito

```bash
# Obtener coordenadas del depósito 1
curl -X GET "$API_URL/api/v1/depositos/1/coordenadas" \
  -H "Authorization: Bearer $TOKEN"

# Respuesta esperada:
# {
#   "depositoId": 1,
#   "nombre": "Depósito Central Buenos Aires",
#   "latitud": -34.6037,
#   "longitud": -58.3816
# }
```

### 4. Calcular Distancia Entre Depósitos

```bash
# Calcular distancia entre depósito 1 y 2
curl -X POST "$API_URL/api/v1/gestion/distancia" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "origen": "1",
    "destino": "2"
  }'

# Respuesta esperada:
# {
#   "distancia": 298.12,
#   "duracion": 3.2
# }
```

### 5. Calcular Ruta Tentativa Directa

```bash
# Ruta directa entre depósito 1 y 3
curl -X GET "$API_URL/api/v1/rutas/tentativa?origenDepositoId=1&destinoDepositoId=3" \
  -H "Authorization: Bearer $TOKEN"

# Respuesta esperada:
# {
#   "depositosIds": [1, 3],
#   "depositosNombres": ["Depósito Central Buenos Aires", "Depósito Córdoba Centro"],
#   "distanciaTotal": 698.45,
#   "duracionTotalHoras": 8.1,
#   "numeroTramos": 1,
#   "tramos": [
#     {
#       "orden": 1,
#       "origenDepositoId": 1,
#       "origenDepositoNombre": "Depósito Central Buenos Aires",
#       "destinoDepositoId": 3,
#       "destinoDepositoNombre": "Depósito Córdoba Centro",
#       "distanciaKm": 698.45,
#       "duracionHoras": 8.1
#     }
#   ],
#   "exitoso": true,
#   "mensaje": "Ruta tentativa calculada exitosamente con 1 tramos"
# }
```

### 6. Calcular Ruta Tentativa con Depósito Intermedio

```bash
# Ruta con parada en Rosario (depósito 2)
curl -X GET "$API_URL/api/v1/rutas/tentativa?origenDepositoId=1&destinoDepositoId=3&intermedios=2" \
  -H "Authorization: Bearer $TOKEN"

# Respuesta esperada:
# {
#   "depositosIds": [1, 2, 3],
#   "depositosNombres": [
#     "Depósito Central Buenos Aires", 
#     "Depósito Rosario Norte",
#     "Depósito Córdoba Centro"
#   ],
#   "distanciaTotal": 701.34,
#   "duracionTotalHoras": 8.5,
#   "numeroTramos": 2,
#   "tramos": [
#     {
#       "orden": 1,
#       "origenDepositoId": 1,
#       "destinoDepositoId": 2,
#       "distanciaKm": 298.12,
#       "duracionHoras": 3.2
#     },
#     {
#       "orden": 2,
#       "origenDepositoId": 2,
#       "destinoDepositoId": 3,
#       "distanciaKm": 403.22,
#       "duracionHoras": 5.3
#     }
#   ],
#   "exitoso": true,
#   "mensaje": "Ruta tentativa calculada exitosamente con 2 tramos"
# }
```

### 7. Crear Ruta Real Basada en Ruta Tentativa

```bash
# Primero crear una solicitud
SOLICITUD_ID=$(curl -X POST "$API_URL/api/v1/solicitudes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "direccionOrigen": "Buenos Aires",
    "direccionDestino": "Córdoba"
  }' | jq -r '.id')

echo "Solicitud creada con ID: $SOLICITUD_ID"

# Crear la ruta
RUTA_ID=$(curl -X POST "$API_URL/api/v1/rutas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"idSolicitud\": $SOLICITUD_ID
  }" | jq -r '.id')

echo "Ruta creada con ID: $RUTA_ID"

# Crear primer tramo (Buenos Aires -> Rosario)
curl -X POST "$API_URL/api/v1/rutas/$RUTA_ID/tramos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "origenDepositoId": 1,
    "destinoDepositoId": 2
  }'

# Crear segundo tramo (Rosario -> Córdoba)
curl -X POST "$API_URL/api/v1/rutas/$RUTA_ID/tramos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "origenDepositoId": 2,
    "destinoDepositoId": 3
  }'

echo "Tramos creados exitosamente"
```

### 8. Verificar Tramos Creados

```bash
# Listar tramos de la ruta
curl -X GET "$API_URL/api/v1/tramos/por-ruta/$RUTA_ID" \
  -H "Authorization: Bearer $TOKEN"
```

## ✅ Validaciones Esperadas

### ✓ Calcular distancia con depósitos no existentes
```bash
curl -X POST "$API_URL/api/v1/gestion/distancia" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "origen": "9999",
    "destino": "9998"
  }'

# Debería devolver error 500 con mensaje: "Depósito no encontrado con id: 9999"
```

### ✓ Ruta tentativa con depósitos inexistentes
```bash
curl -X GET "$API_URL/api/v1/rutas/tentativa?origenDepositoId=9999&destinoDepositoId=9998" \
  -H "Authorization: Bearer $TOKEN"

# Debería devolver:
# {
#   "exitoso": false,
#   "mensaje": "Error al calcular ruta: ..."
# }
```

### ✓ Depósito sin coordenadas
```bash
# Crear depósito sin coordenadas
DEPOSITO_ID=$(curl -X POST "$API_URL/api/v1/depositos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Depósito Sin Coordenadas",
    "direccion": "Calle Falsa 123"
  }' | jq -r '.id')

# Intentar calcular distancia
curl -X POST "$API_URL/api/v1/gestion/distancia" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"origen\": \"$DEPOSITO_ID\",
    \"destino\": \"1\"
  }"

# Debería devolver error: "El depósito ID X no tiene coordenadas configuradas"
```

## 🎯 Resultados Esperados

Si todas las pruebas pasan correctamente:

1. ✅ Los depósitos se consultan correctamente por ID
2. ✅ Las coordenadas se obtienen desde la base de datos
3. ✅ OSRM calcula distancias reales (no valores por defecto)
4. ✅ Las rutas tentativas incluyen múltiples depósitos
5. ✅ Los tramos se crean con distancias correctas
6. ✅ Las validaciones funcionan apropiadamente

## 🔧 Troubleshooting

Si las distancias siguen siendo incorrectas:

1. Verificar que OSRM esté corriendo:
   ```bash
   docker ps | grep osrm
   ```

2. Verificar logs del microservicio de cálculos:
   ```bash
   docker logs tpi-backend-ms-gestion-calculos-1
   ```

3. Verificar que los depósitos tienen coordenadas:
   ```bash
   curl -X GET "$API_URL/api/v1/depositos" -H "Authorization: Bearer $TOKEN" | jq
   ```
