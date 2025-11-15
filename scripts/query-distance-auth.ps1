<#
Query OSRM-backed maps/distancia-osrm using token from token.txt
Usage examples:
  # default gateway (localhost:8080) and default token.txt in repo root
  .\scripts\query-distance-auth.ps1 -FromLat -34.603722 -FromLon -58.381592 -ToLat -34.608147 -ToLon -58.370226

  # specify token file and gateway
  .\scripts\query-distance-auth.ps1 -TokenFile .\\token.txt -GatewayUrl http://localhost:8082
#>
param(
    [Parameter(Mandatory=$true)] [double]$FromLat,
    [Parameter(Mandatory=$true)] [double]$FromLon,
    [Parameter(Mandatory=$true)] [double]$ToLat,
    [Parameter(Mandatory=$true)] [double]$ToLon,
    [string]$TokenFile = "token.txt",
    [string]$GatewayUrl = "http://localhost:8080"
)

function Fail($msg) { Write-Host "ERROR: $msg" -ForegroundColor Red; exit 1 }

if (-not (Test-Path $TokenFile)) { Fail "Token file not found: $TokenFile" }

$token = (Get-Content $TokenFile -Raw).Trim()
if ([string]::IsNullOrWhiteSpace($token)) { Fail "Token file is empty" }

# Format numbers using invariant culture (force dot decimal separator)
$ci = [System.Globalization.CultureInfo]::InvariantCulture
$fromLatStr = $FromLat.ToString($ci)
$fromLonStr = $FromLon.ToString($ci)
$toLatStr = $ToLat.ToString($ci)
$toLonStr = $ToLon.ToString($ci)

# Build URL with invariant-formatted coordinates
$uri = "{0}/api/v1/maps/distancia-osrm?origenLat={1}&origenLon={2}&destinoLat={3}&destinoLon={4}" -f $GatewayUrl, $fromLatStr, $fromLonStr, $toLatStr, $toLonStr

Write-Host "Calling: $uri" -ForegroundColor Cyan

try {
    $headers = @{ Authorization = "Bearer $token" }
    $resp = Invoke-RestMethod -Uri $uri -Headers $headers -Method Get -ErrorAction Stop

    if ($null -eq $resp) {
        Write-Host "No response received." -ForegroundColor Yellow
        exit 0
    }

    # Distancia en km y duracion en horas (según DistanciaResponseDTO)
    $distanciaKm = $resp.distancia
    $duracionHoras = $resp.duracion

    if ($distanciaKm -ne $null) {
        "Distance: {0:N2} km" -f $distanciaKm
    } else {
        Write-Host "Distance not present in response." -ForegroundColor Yellow
    }

    if ($duracionHoras -ne $null) {
        $duracionMin = $duracionHoras * 60.0
        "Duration: {0:N2} hours ({1:N1} minutes)" -f $duracionHoras, $duracionMin
    } else {
        Write-Host "Duration not present in response." -ForegroundColor Yellow
    }

} catch {
    Write-Host "Request failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try { $_.Exception.Response | Format-List -Force } catch {}
    }
    exit 1
}
