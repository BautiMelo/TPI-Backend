# Script de pruebas simplificado para TPI Backend
$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8080'
$tokenEndpoint = 'http://localhost:8089/realms/tpi-backend/protocol/openid-connect/token'

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "TPI Backend - Pruebas de Endpoints" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

# Obtener token de autenticación
Write-Host "[1] AUTENTICACION" -ForegroundColor Cyan
Write-Host "Obteniendo token para usuario 'tester'..." -ForegroundColor White

try {
    $body = @{ 
        grant_type='password'
        client_id='postman-test'
        client_secret='secret-postman-123'
        username='tester'
        password='1234'
    }
    $tokenResp = Invoke-RestMethod -Method Post -Uri $tokenEndpoint -Body $body -ContentType 'application/x-www-form-urlencoded' -ErrorAction Stop
    $token = $tokenResp.access_token
    Write-Host "Token obtenido exitosamente" -ForegroundColor Green
} catch {
    Write-Host "Error obteniendo token: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Esperando mas tiempo para que Keycloak se inicialice..." -ForegroundColor Yellow
    Start-Sleep -Seconds 20
    try {
        $tokenResp = Invoke-RestMethod -Method Post -Uri $tokenEndpoint -Body $body -ContentType 'application/x-www-form-urlencoded' -ErrorAction Stop
        $token = $tokenResp.access_token
        Write-Host "Token obtenido exitosamente en segundo intento" -ForegroundColor Green
    } catch {
        Write-Host "No se pudo obtener token. Verifique que Keycloak este funcionando." -ForegroundColor Red
        exit 1
    }
}

$headers = @{ 
    Authorization = "Bearer $token"
    'Content-Type' = 'application/json'
}

# Pruebas MS Solicitudes
Write-Host "`n[2] MS SOLICITUDES" -ForegroundColor Cyan

Write-Host "GET /api/v1/solicitudes - Listar solicitudes" -ForegroundColor White
try {
    $resp = Invoke-RestMethod -Uri "$base/api/v1/solicitudes" -Headers $headers -Method Get
    Write-Host "OK - Solicitudes listadas correctamente" -ForegroundColor Green
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nPOST /api/v1/solicitudes - Crear solicitud" -ForegroundColor White
try {
    $createBody = @{
        direccionOrigen = "Av. Corrientes 1234, CABA"
        direccionDestino = "Av. Santa Fe 5678, CABA"
    } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$base/api/v1/solicitudes" -Headers $headers -Method Post -Body $createBody
    Write-Host "OK - Solicitud creada con ID: $($resp.id)" -ForegroundColor Green
    $solicitudId = $resp.id
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

if ($solicitudId) {
    Write-Host "`nGET /api/v1/solicitudes/$solicitudId - Obtener solicitud por ID" -ForegroundColor White
    try {
        $resp = Invoke-RestMethod -Uri "$base/api/v1/solicitudes/$solicitudId" -Headers $headers -Method Get
        Write-Host "OK - Solicitud ID $solicitudId obtenida correctamente" -ForegroundColor Green
    } catch {
        Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Pruebas MS Gestion Calculos
Write-Host "`n[3] MS GESTION CALCULOS" -ForegroundColor Cyan

Write-Host "GET /api/v1/tarifas - Listar tarifas" -ForegroundColor White
try {
    $resp = Invoke-RestMethod -Uri "$base/api/v1/tarifas" -Headers $headers -Method Get
    Write-Host "OK - Tarifas listadas correctamente" -ForegroundColor Green
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nPOST /api/v1/tarifas - Crear tarifa" -ForegroundColor White
try {
    $tarifaBody = @{
        nombre = "Tarifa Test"
        precioPorKm = 125.50
        activa = $true
    } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$base/api/v1/tarifas" -Headers $headers -Method Post -Body $tarifaBody
    Write-Host "OK - Tarifa creada con ID: $($resp.id)" -ForegroundColor Green
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nGET /api/v1/depositos - Listar depositos" -ForegroundColor White
try {
    $resp = Invoke-RestMethod -Uri "$base/api/v1/depositos" -Headers $headers -Method Get
    Write-Host "OK - Depositos listados correctamente" -ForegroundColor Green
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Pruebas MS Rutas Transportistas
Write-Host "`n[4] MS RUTAS TRANSPORTISTAS" -ForegroundColor Cyan

Write-Host "GET /api/v1/rutas - Listar rutas" -ForegroundColor White
try {
    $resp = Invoke-RestMethod -Uri "$base/api/v1/rutas" -Headers $headers -Method Get
    Write-Host "OK - Rutas listadas correctamente" -ForegroundColor Green
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nGET /api/v1/camiones - Listar camiones" -ForegroundColor White
try {
    $resp = Invoke-RestMethod -Uri "$base/api/v1/camiones" -Headers $headers -Method Get
    Write-Host "OK - Camiones listados correctamente" -ForegroundColor Green
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nGET /api/v1/tramos - Listar tramos" -ForegroundColor White
try {
    $resp = Invoke-RestMethod -Uri "$base/api/v1/tramos" -Headers $headers -Method Get
    Write-Host "OK - Tramos listados correctamente" -ForegroundColor Green
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Prueba OSRM
Write-Host "`n[5] OSRM - CALCULO DE RUTAS" -ForegroundColor Cyan
Write-Host "POST /api/v1/osrm/ruta - Calcular ruta con OSRM" -ForegroundColor White
try {
    $osrmBody = @{
        origen = @{
            latitud = -34.603722
            longitud = -58.381592
        }
        destino = @{
            latitud = -34.608147
            longitud = -58.370226
        }
    } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$base/api/v1/osrm/ruta" -Headers $headers -Method Post -Body $osrmBody
    Write-Host "OK - Ruta calculada: distancia = $($resp.distanciaKm) km, duracion = $($resp.duracionMinutos) min" -ForegroundColor Green
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "Pruebas completadas" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow
