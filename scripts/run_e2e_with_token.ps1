# Helper script: obtiene token admin desde Keycloak y ejecuta el script e2e_test.ps1
param(
    [string]$KeycloakUrl = 'http://localhost:8089',
    [string]$ClientId = 'postman-test',
    [string]$ClientSecret = 'secret-postman-123',
    [string]$Username = 'tester',
    [string]$Password = '1234',
    [string]$BaseUrl = 'http://localhost:8080'
)

Set-Location (Split-Path -Parent $MyInvocation.MyCommand.Definition)
Set-Location ..\
$ErrorActionPreference = 'Stop'

Write-Output "Obteniendo token desde Keycloak ($KeycloakUrl)..."
$tokenResp = Invoke-RestMethod -Method Post -Uri "$KeycloakUrl/realms/tpi-backend/protocol/openid-connect/token" -Body @{ grant_type='password'; client_id=$ClientId; client_secret=$ClientSecret; username=$Username; password=$Password } -ContentType 'application/x-www-form-urlencoded'
if (-not $tokenResp -or -not $tokenResp.access_token) {
    Write-Error "No se obtuvo access_token desde Keycloak"
    exit 1
}
$preview = $tokenResp.access_token.Substring(0,[Math]::Min(20,$tokenResp.access_token.Length)) + '...'
Write-Output "Token obtenido (preview): $preview"

Write-Output "Lanzando script E2E..."
& "${PWD}\scripts\e2e_test.ps1" -SOLICITUDES_BASE $BaseUrl -RUTAS_BASE $BaseUrl -TOKEN $tokenResp.access_token
$exitCode = $LASTEXITCODE
Write-Output "E2E script terminó con código: $exitCode"
if ($exitCode -ne 0) { exit $exitCode }
Write-Output "Helper completo."
