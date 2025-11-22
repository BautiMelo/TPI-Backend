# Obtiene token y decodifica payload (sin mostrar token completo)
param(
    [string]$KeycloakUrl = 'http://localhost:8089',
    [string]$ClientId = 'postman-test',
    [string]$ClientSecret = 'secret-postman-123',
    [string]$Username = 'tester',
    [string]$Password = '1234'
)
Set-Location (Split-Path -Parent $MyInvocation.MyCommand.Definition)
Set-Location ..\
$ErrorActionPreference = 'Stop'
Write-Output "Pidiendo token a $KeycloakUrl..."
$tokenResp = Invoke-RestMethod -Method Post -Uri "$KeycloakUrl/realms/tpi-backend/protocol/openid-connect/token" -Body @{ grant_type='password'; client_id=$ClientId; client_secret=$ClientSecret; username=$Username; password=$Password } -ContentType 'application/x-www-form-urlencoded'
if (-not $tokenResp -or -not $tokenResp.access_token) { Write-Error 'No se obtuvo access_token'; exit 1 }
$tok = $tokenResp.access_token
# Decode payload
$parts = $tok.Split('.')
if ($parts.Length -lt 2) { Write-Error 'Token malformed'; exit 1 }
$payload = $parts[1]
# base64url -> base64
$payload = $payload.Replace('-','+').Replace('_','/')
$pad = 4 - ($payload.Length % 4)
if ($pad -lt 4) { $payload += '=' * $pad }
$bytes = [System.Convert]::FromBase64String($payload)
$json = [System.Text.Encoding]::UTF8.GetString($bytes)
Write-Output '--- Decoded token payload JSON ---'
Write-Output $json
# Try to parse and show common claims
try {
    $obj = $json | ConvertFrom-Json
    Write-Output 'realm_access.roles (if present):'
    if ($obj.realm_access -ne $null -and $obj.realm_access.roles -ne $null) { $obj.realm_access.roles } else { Write-Output '(none)' }
    Write-Output 'resource_access (if present):'
    if ($obj.resource_access -ne $null) { $obj.resource_access | ConvertTo-Json -Depth 3 } else { Write-Output '(none)' }
} catch {
    Write-Error 'No se pudo parsear JSON del payload'
}
