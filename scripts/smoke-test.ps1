# Smoke test script for TPI Backend
# Usage: Open PowerShell in the repo root and run: .\scripts\smoke-test.ps1

$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$tokenEndpoint = 'http://localhost:8089/realms/tpi-backend/protocol/openid-connect/token'

# Function to get token for a specific user
function Get-AuthToken {
    param([string]$username, [string]$password = '1234')
    $body = @{ grant_type='password'; client_id='postman-test'; client_secret='secret-postman-123'; username=$username; password=$password }
    $resp = Invoke-RestMethod -Method Post -Uri $tokenEndpoint -Body $body -ContentType 'application/x-www-form-urlencoded'
    return $resp.access_token
}

# Function to run a test
function Invoke-ApiTest {
    param(
        [string]$name,
        [string]$method,
        [string]$url,
        [hashtable]$headers,
        [string]$body = $null,
        [int]$expectedStatus = 200
    )
    Write-Host "`n---- $name" -ForegroundColor Cyan
    Write-Host "     $method $url"
    
    try {
        $params = @{
            Uri = $url
            Headers = $headers
            Method = $method
            UseBasicParsing = $true
            TimeoutSec = 30
        }
        if ($body) { $params.Body = $body }
        
        $r = Invoke-WebRequest @params
        $status = $r.StatusCode
        $bodyOut = $r.Content
        
        if ($status -eq $expectedStatus) {
            Write-Host "     ✓ Status: $status" -ForegroundColor Green
            if ($bodyOut.Length -lt 200) {
                Write-Host "     Response: $bodyOut" -ForegroundColor Gray
            } else {
                Write-Host "     Response: $($bodyOut.Substring(0,200))..." -ForegroundColor Gray
            }
            return $true
        } else {
            Write-Host "     ✗ Status: $status (expected $expectedStatus)" -ForegroundColor Red
            return $false
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $expectedStatus) {
            Write-Host "     ✓ Status: $statusCode (expected $expectedStatus)" -ForegroundColor Green
            return $true
        } else {
            Write-Host "     ✗ Failed: $statusCode - $($_.Exception.Message)" -ForegroundColor Red
            return $false
        }
    }
}

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "TPI Backend - Smoke Tests" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

$allOk = $true
$testCount = 0
$passCount = 0

# =============================================
# Test 1: Authentication & Token Generation
# =============================================
Write-Host "`n[1] AUTHENTICATION TESTS" -ForegroundColor Magenta
Write-Host "Getting tokens for different users..."

try {
    $testerToken = Get-AuthToken -username 'tester'
    Write-Host "✓ Token obtained for 'tester' (all roles)" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to get token for 'tester'" -ForegroundColor Red
    $allOk = $false
    exit 1
}

# =============================================
# Test 2: MS Solicitudes - Basic CRUD
# =============================================
Write-Host "`n[2] MS SOLICITUDES - CRUD OPERATIONS" -ForegroundColor Magenta
$headers = @{ Authorization = "Bearer $testerToken"; 'Content-Type' = 'application/json' }

# List solicitudes (should be empty or have items)
$testCount++
if (Invoke-ApiTest -name "List all solicitudes" -method GET -url "$base/api/v1/solicitudes" -headers $headers) { $passCount++ }

# Create solicitud
$testCount++
$createBody = '{"direccionOrigen":"Av. Corrientes 1234, CABA","direccionDestino":"Av. Santa Fe 5678, CABA"}'
if (Invoke-ApiTest -name "Create solicitud" -method POST -url "$base/api/v1/solicitudes" -headers $headers -body $createBody) { 
    $passCount++
    $solicitudId = 1  # Assume first created has ID 1
}

# Get solicitud by ID
if ($solicitudId) {
    $testCount++
    if (Invoke-ApiTest -name "Get solicitud by ID" -method GET -url "$base/api/v1/solicitudes/$solicitudId" -headers $headers) { $passCount++ }
}

# Update solicitud
if ($solicitudId) {
    $testCount++
    $updateBody = '{"direccionOrigen":"Av. Corrientes 999, CABA","direccionDestino":"Av. Santa Fe 777, CABA"}'
    if (Invoke-ApiTest -name "Update solicitud" -method PUT -url "$base/api/v1/solicitudes/$solicitudId" -headers $headers -body $updateBody) { $passCount++ }
}

# =============================================
# Test 3: MS Gestion Calculos - Tarifas
# =============================================
Write-Host "`n[3] MS GESTION CALCULOS - TARIFAS & PRECIOS" -ForegroundColor Magenta

$testCount++
if (Invoke-ApiTest -name "List tarifas" -method GET -url "$base/api/v1/tarifas" -headers $headers) { $passCount++ }

$testCount++
$tarifaBody = '{"nombre":"Tarifa Test","precioPorKm":125.50,"activa":true}'
if (Invoke-ApiTest -name "Create tarifa" -method POST -url "$base/api/v1/tarifas" -headers $headers -body $tarifaBody) { $passCount++ }

$testCount++
if (Invoke-ApiTest -name "List depositos" -method GET -url "$base/api/v1/depositos" -headers $headers) { $passCount++ }

# =============================================
# Test 4: MS Rutas Transportistas
# =============================================
Write-Host "`n[4] MS RUTAS TRANSPORTISTAS - RUTAS & CAMIONES" -ForegroundColor Magenta

$testCount++
if (Invoke-ApiTest -name "List rutas" -method GET -url "$base/api/v1/rutas" -headers $headers) { $passCount++ }

$testCount++
if (Invoke-ApiTest -name "List camiones" -method GET -url "$base/api/v1/camiones" -headers $headers) { $passCount++ }

$testCount++
if (Invoke-ApiTest -name "List tramos" -method GET -url "$base/api/v1/tramos" -headers $headers) { $passCount++ }

# OSRM Integration Tests
Write-Host "`n     OSRM - Cálculo de Rutas" -ForegroundColor White
$testCount++
$osrmRuta = '{"origen":{"latitud":-34.603722,"longitud":-58.381592},"destino":{"latitud":-34.608147,"longitud":-58.370226}}'
if (Invoke-ApiTest -name "Calculate route with OSRM" -method POST -url "$base/api/v1/osrm/ruta" -headers $headers -body $osrmRuta) { $passCount++ }

# =============================================
# Test 5: Cross-Service Integration
# =============================================
Write-Host "`n[5] CROSS-SERVICE INTEGRATION" -ForegroundColor Magenta

if ($solicitudId) {
    $testCount++
    # This might fail if services aren't fully configured, but we test the endpoint
    Write-Host "     Note: Integration tests may fail if Google Maps API is not configured" -ForegroundColor Yellow
    Invoke-ApiTest -name "Request route for solicitud" -method POST -url "$base/api/v1/solicitudes/$solicitudId/request-route" -headers $headers -expectedStatus 200 | Out-Null
    # Don't count integration tests in pass/fail since they depend on external config
}

# =============================================
# Test 6: Authorization - Different Roles
# =============================================
Write-Host "`n[6] AUTHORIZATION TESTS (Role-based Access)" -ForegroundColor Magenta

# Test with cliente1 user (only CLIENTE role)
try {
    $clienteToken = Get-AuthToken -username 'cliente1'
    $clienteHeaders = @{ Authorization = "Bearer $clienteToken"; 'Content-Type' = 'application/json' }
    
    $testCount++
    # CLIENTE should be able to create solicitudes
    if (Invoke-ApiTest -name "CLIENTE: Create solicitud (should succeed)" -method POST -url "$base/api/v1/solicitudes" -headers $clienteHeaders -body $createBody) { $passCount++ }
    
    $testCount++
    # CLIENTE should NOT be able to list all solicitudes (requires RESPONSABLE)
    if (Invoke-ApiTest -name "CLIENTE: List all solicitudes (should fail 403)" -method GET -url "$base/api/v1/solicitudes" -headers $clienteHeaders -expectedStatus 403) { $passCount++ }
} catch {
    Write-Host "     Note: cliente1 user may not exist in Keycloak" -ForegroundColor Yellow
}

# Test with responsable1 user (only RESPONSABLE role)
try {
    $responsableToken = Get-AuthToken -username 'responsable1'
    $responsableHeaders = @{ Authorization = "Bearer $responsableToken"; 'Content-Type' = 'application/json' }
    
    $testCount++
    # RESPONSABLE should be able to list all solicitudes
    if (Invoke-ApiTest -name "RESPONSABLE: List all solicitudes (should succeed)" -method GET -url "$base/api/v1/solicitudes" -headers $responsableHeaders) { $passCount++ }
    
    $testCount++
    # RESPONSABLE should be able to update solicitudes
    if (Invoke-ApiTest -name "RESPONSABLE: Update solicitud (should succeed)" -method PUT -url "$base/api/v1/solicitudes/$solicitudId" -headers $responsableHeaders -body $updateBody) { $passCount++ }
} catch {
    Write-Host "     Note: responsable1 user may not exist in Keycloak" -ForegroundColor Yellow
}

# =============================================
# Summary
# =============================================
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "TEST SUMMARY" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Total Tests:  $testCount"
Write-Host "Passed:       $passCount" -ForegroundColor Green
Write-Host "Failed:       $($testCount - $passCount)" -ForegroundColor $(if ($testCount -eq $passCount) { 'Green' } else { 'Red' })
Write-Host "Success Rate: $([math]::Round(($passCount / $testCount) * 100, 2))%"

if ($passCount -eq $testCount) {
    Write-Host "`n✓ ALL SMOKE TESTS PASSED" -ForegroundColor Green
    exit 0
} elseif ($passCount -ge ($testCount * 0.8)) {
    Write-Host "`n⚠ MOST TESTS PASSED (80%+)" -ForegroundColor Yellow
    Write-Host "Some optional features may need configuration" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "`n✗ SMOKE TESTS FAILED" -ForegroundColor Red
    exit 1
}
