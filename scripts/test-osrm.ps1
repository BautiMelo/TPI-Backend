# Script para probar la integración con OSRM
# Uso: .\scripts\test-osrm.ps1

$ErrorActionPreference = 'Stop'

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "Test OSRM Integration" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

# 1. Obtener token
Write-Host "[1] Obteniendo token de autenticación..." -ForegroundColor Cyan
$body = @{
    grant_type='password'
    client_id='postman-test'
    client_secret='secret-postman-123'
    username='tester'
    password='1234'
}
try {
    $resp = Invoke-RestMethod -Uri "http://localhost:8089/realms/tpi-backend/protocol/openid-connect/token" -Method Post -Body $body
    $token = $resp.access_token
    Write-Host "✓ Token obtenido" -ForegroundColor Green
} catch {
    Write-Host "✗ Error al obtener token: $_" -ForegroundColor Red
    exit 1
}

$headers = @{ 
    Authorization = "Bearer $token"
    'Content-Type' = 'application/json'
}

# 2. Test: Ruta simple (Obelisco a Casa Rosada)
Write-Host "`n[2] Test: Ruta Simple (Obelisco → Casa Rosada)" -ForegroundColor Cyan
$rutaSimple = @{
    origen = @{ 
        latitud = -34.603722
        longitud = -58.381592 
    }
    destino = @{ 
        latitud = -34.608147
        longitud = -58.370226 
    }
} | ConvertTo-Json

try {
    $resultado = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/osrm/ruta" -Method Post -Headers $headers -Body $rutaSimple
    if ($resultado.exitoso) {
        Write-Host "✓ Ruta calculada exitosamente" -ForegroundColor Green
        Write-Host "  Distancia: $($resultado.distanciaKm) km" -ForegroundColor Gray
        Write-Host "  Duración: $($resultado.duracionMinutos) minutos" -ForegroundColor Gray
        Write-Host "  Resumen: $($resultado.resumen)" -ForegroundColor Gray
    } else {
        Write-Host "✗ Fallo al calcular ruta: $($resultado.mensaje)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Error: $_" -ForegroundColor Red
}

# 3. Test: Ruta con GET
Write-Host "`n[3] Test: Ruta Simple (GET) (Palermo → Puerto Madero)" -ForegroundColor Cyan
try {
    $url = "http://localhost:8080/api/v1/osrm/ruta-simple?origenLat=-34.588889&origenLon=-58.421944&destinoLat=-34.611667&destinoLon=-58.361944"
    $resultado = Invoke-RestMethod -Uri $url -Method Get -Headers @{ Authorization = "Bearer $token" }
    if ($resultado.exitoso) {
        Write-Host "✓ Ruta calculada exitosamente" -ForegroundColor Green
        Write-Host "  Distancia: $($resultado.distanciaKm) km" -ForegroundColor Gray
        Write-Host "  Duración: $($resultado.duracionMinutos) minutos" -ForegroundColor Gray
    } else {
        Write-Host "✗ Fallo al calcular ruta: $($resultado.mensaje)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Error: $_" -ForegroundColor Red
}

# 4. Test: Ruta múltiple (Tour por Buenos Aires)
Write-Host "`n[4] Test: Ruta Múltiple (Obelisco → Casa Rosada → Puerto Madero)" -ForegroundColor Cyan
$rutaMultiple = @{
    coordenadas = @(
        @{ latitud = -34.603722; longitud = -58.381592 },  # Obelisco
        @{ latitud = -34.608147; longitud = -58.370226 },  # Casa Rosada
        @{ latitud = -34.611667; longitud = -58.361944 }   # Puerto Madero
    )
} | ConvertTo-Json -Depth 5

try {
    $resultado = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/osrm/ruta-multiple" -Method Post -Headers $headers -Body $rutaMultiple
    if ($resultado.exitoso) {
        Write-Host "✓ Ruta múltiple calculada exitosamente" -ForegroundColor Green
        Write-Host "  Distancia total: $($resultado.distanciaKm) km" -ForegroundColor Gray
        Write-Host "  Duración total: $($resultado.duracionMinutos) minutos" -ForegroundColor Gray
        Write-Host "  Resumen: $($resultado.resumen)" -ForegroundColor Gray
    } else {
        Write-Host "✗ Fallo al calcular ruta múltiple: $($resultado.mensaje)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Error: $_" -ForegroundColor Red
}

# 5. Test: Endpoint simplificado (solo distancia/duración)
Write-Host "`n[5] Test: Distancia OSRM (Córdoba Centro → Nueva Córdoba)" -ForegroundColor Cyan
try {
    $url = "http://localhost:8080/api/v1/maps/distancia-osrm?origenLat=-31.420083&origenLon=-64.188776&destinoLat=-31.423889&destinoLon=-64.188889"
    $resultado = Invoke-RestMethod -Uri $url -Method Get -Headers @{ Authorization = "Bearer $token" }
    Write-Host "✓ Distancia calculada" -ForegroundColor Green
    Write-Host "  Distancia: $($resultado.distancia) km" -ForegroundColor Gray
    Write-Host "  Duración: $($resultado.duracion) horas" -ForegroundColor Gray
} catch {
    Write-Host "✗ Error: $_" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "✓ Tests completados" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Yellow
