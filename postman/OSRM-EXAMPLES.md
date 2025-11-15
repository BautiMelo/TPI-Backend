# Guía Rápida - Ejemplos OSRM en Postman

## 📦 Importar en Postman

Si aún no lo hiciste:
1. Import → `postman/TPI-Backend-APIs.postman_collection.json`
2. Import → `postman/TPI-Backend.postman_environment.json`
3. Seleccionar environment "TPI-Backend Local"

## 🔑 Autenticación Automática

La colección ya tiene un **prerequest script** que obtiene tokens automáticamente. Solo ejecuta cualquier request.

## 📍 Ejemplos Listos para Usar

### 1️⃣ Ruta Simple: Obelisco → Casa Rosada

**Request**:
```
POST http://localhost:8080/api/v1/osrm/ruta
```

**Headers**:
```
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

**Body (JSON)**:
```json
{
  "origen": {
    "latitud": -34.603722,
    "longitud": -58.381592
  },
  "destino": {
    "latitud": -34.608147,
    "longitud": -58.370226
  }
}
```

**Respuesta Esperada**:
```json
{
  "distanciaKm": 1.34,
  "duracionHoras": 0.07,
  "duracionMinutos": 4.2,
  "geometry": "oav~FltazGZhAv@tBb@pA\\~@...",
  "resumen": "Av. Corrientes",
  "exitoso": true,
  "mensaje": "Ruta calculada exitosamente"
}
```

---

### 2️⃣ Tour por Buenos Aires (Múltiples Paradas)

**Request**:
```
POST http://localhost:8080/api/v1/osrm/ruta-multiple
```

**Body (JSON)**:
```json
{
  "coordenadas": [
    {
      "latitud": -34.603722,
      "longitud": -58.381592,
      "nombre": "Obelisco"
    },
    {
      "latitud": -34.608147,
      "longitud": -58.370226,
      "nombre": "Casa Rosada"
    },
    {
      "latitud": -34.611667,
      "longitud": -58.361944,
      "nombre": "Puerto Madero"
    },
    {
      "latitud": -34.588889,
      "longitud": -58.421944,
      "nombre": "Palermo"
    }
  ]
}
```

**Respuesta Esperada**:
```json
{
  "distanciaKm": 12.45,
  "duracionHoras": 0.58,
  "duracionMinutos": 34.8,
  "geometry": "encoded_polyline_for_entire_route",
  "resumen": "4 puntos visitados",
  "exitoso": true,
  "mensaje": "Ruta calculada exitosamente"
}
```

---

### 3️⃣ Solo Distancia (GET simplificado)

**Request**:
```
GET http://localhost:8080/api/v1/maps/distancia-osrm?origenLat=-34.603722&origenLon=-58.381592&destinoLat=-34.608147&destinoLon=-58.370226
```

**Respuesta Esperada**:
```json
{
  "distancia": 1.34,
  "duracion": 0.07
}
```

---

## 🎨 Visualizar la Ruta

La respuesta incluye un campo `geometry` con la ruta codificada. Puedes decodificarla:

### Opción 1: Google Maps Polyline Decoder
https://developers.google.com/maps/documentation/utilities/polylineutility

### Opción 2: Leaflet (JavaScript)
```javascript
var polyline = L.Polyline.decode(geometry);
map.addLayer(polyline);
```

### Opción 3: Online Tool
https://www.javawa.nl/polydecode.html

---

## 🧪 Tests en Postman

Puedes agregar **Tests** en cada request para validar automáticamente:

```javascript
// Test 1: Verificar que la respuesta es exitosa
pm.test("Ruta calculada exitosamente", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.exitoso).to.be.true;
});

// Test 2: Validar que tiene distancia
pm.test("Tiene distancia en km", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.distanciaKm).to.be.above(0);
});

// Test 3: Validar que tiene duración
pm.test("Tiene duración en minutos", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.duracionMinutos).to.be.above(0);
});

// Test 4: Validar geometría
pm.test("Tiene geometría de ruta", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.geometry).to.be.a('string');
    pm.expect(jsonData.geometry.length).to.be.above(0);
});
```

---

## 📊 Variables de Entorno Útiles

Puedes crear estas variables en tu environment para reutilizarlas:

```json
{
  "obelisco_lat": "-34.603722",
  "obelisco_lon": "-58.381592",
  "casa_rosada_lat": "-34.608147",
  "casa_rosada_lon": "-58.370226",
  "puerto_madero_lat": "-34.611667",
  "puerto_madero_lon": "-58.361944"
}
```

Luego usa en el body:
```json
{
  "origen": {
    "latitud": {{obelisco_lat}},
    "longitud": {{obelisco_lon}}
  },
  "destino": {
    "latitud": {{casa_rosada_lat}},
    "longitud": {{casa_rosada_lon}}
  }
}
```

---

## 🚨 Manejo de Errores

### Error 1: Coordenadas Inválidas
```json
{
  "exitoso": false,
  "mensaje": "No se pudo calcular la ruta. Código: NoRoute"
}
```

### Error 2: Autenticación Fallida (401)
- Verificar que el token no haya expirado
- Re-ejecutar el request de "Obtener Token"

### Error 3: Sin Permisos (403)
- El usuario debe tener rol RESPONSABLE, TRANSPORTISTA o ADMIN

---

## 💡 Tips & Tricks

1. **Performance**: OSRM es MUY rápido (50-200ms). Puedes hacer múltiples requests sin problemas.

2. **Caché**: Si calculas la misma ruta muchas veces, considera cachear el resultado.

3. **Precisión**: Las coordenadas usan hasta 6 decimales para máxima precisión (~0.1m).

4. **Geometría**: El polyline codificado es muy compacto. Un string de 200 chars puede representar una ruta de varios km.

5. **Rutas Alternativas**: OSRM siempre retorna la ruta más corta. Para alternativas, necesitarías múltiples requests con waypoints intermedios.

---

## 🔗 Links Útiles

- **OSRM Demo**: http://map.project-osrm.org/
- **API Docs**: http://project-osrm.org/docs/v5.24.0/api/
- **OpenStreetMap**: https://www.openstreetmap.org/
