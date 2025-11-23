# Directorio de Datos OSRM

Este directorio contiene los archivos de datos de **OSRM (Open Source Routing Machine)** necesarios para el cálculo de rutas en Argentina.

## ⚠️ Archivos NO incluidos en Git

Los archivos de datos OSRM son **demasiado grandes para Git** (>100MB cada uno) y están excluidos en `.gitignore`. Debes descargarlos y procesarlos localmente.

## 📥 Cómo obtener los archivos

### Opción 1: Descargar y procesar desde cero (Primera vez)

#### 1. Descargar datos de Argentina
```powershell
# Desde la raíz del proyecto
Invoke-WebRequest -Uri "http://download.geofabrik.de/south-america/argentina-latest.osm.pbf" `
  -OutFile "osrm-data/argentina-latest.osm.pbf"
```

#### 2. Procesar con OSRM (solo una vez)
```powershell
# Extraer datos
docker run -t -v "${PWD}/osrm-data:/data" osrm/osrm-backend osrm-extract -p /opt/car.lua /data/argentina-latest.osm.pbf

# Particionar datos
docker run -t -v "${PWD}/osrm-data:/data" osrm/osrm-backend osrm-partition /data/argentina-latest.osrm

# Personalizar datos
docker run -t -v "${PWD}/osrm-data:/data" osrm/osrm-backend osrm-customize /data/argentina-latest.osrm
```

**Tiempo estimado**: 5-15 minutos dependiendo de tu máquina.

### Opción 2: Copiar de otro desarrollador

Si otro miembro del equipo ya procesó los datos, puedes copiar directamente todos los archivos `.osrm*` a este directorio.

## 📁 Archivos que deberías tener

Después del procesamiento, este directorio debe contener:

```
osrm-data/
├── .gitkeep
├── README.md (este archivo)
├── argentina-latest.osm.pbf         (~500 MB) - Datos originales
├── argentina-latest.osrm             (~800 MB) - Datos procesados
├── argentina-latest.osrm.hsgr        (~400 MB) - Grafo de contracción
├── argentina-latest.osrm.fileIndex   (~1 KB)   - Índice
├── argentina-latest.osrm.geometry    (~200 MB) - Geometría
├── argentina-latest.osrm.names       (~50 MB)  - Nombres de calles
└── [otros archivos .osrm.*]
```

## 🚀 Levantar servidor OSRM

Una vez que tengas los archivos procesados:

```powershell
# Desde la raíz del proyecto
docker-compose -f docker-compose.osrm.yml up -d
```

## ✅ Verificar que funciona

```powershell
# Probar endpoint OSRM directamente
Invoke-RestMethod "http://localhost:5000/route/v1/driving/-58.381592,-34.603722;-58.370226,-34.608147?overview=false"
```

Deberías ver una respuesta JSON con la ruta calculada.

## 📚 Más información

Ver documentación completa en:
- `../OSRM-IMPLEMENTATION.md` - Guía completa de implementación
- `../ms-rutas-transportistas/README-OSRM.md` - Uso de los endpoints

## 🔗 Enlaces útiles

- **Geofabrik Downloads**: http://download.geofabrik.de/south-america.html
- **OSRM Backend Docs**: https://github.com/Project-OSRM/osrm-backend
- **OpenStreetMap Argentina**: https://www.openstreetmap.org/relation/286393
