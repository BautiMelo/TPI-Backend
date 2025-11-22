# E2E manual test script (PowerShell)
# Usa `Invoke-RestMethod` para realizar los pasos: crear solicitud -> solicitar/confirmar ruta -> marcar EN_TRANSITO -> marcar COMPLETADA y finalizar
# Ajusta las URLs y el token según tu entorno.

param(
    # Por defecto usamos el API Gateway. Si quieres apuntar a servicios locales,
    # cambia ambos a "http://localhost:8083" y "http://localhost:8082" respectivamente.
    [string]$SOLICITUDES_BASE = "http://localhost:8080",
    [string]$RUTAS_BASE = "http://localhost:8080",
    [string]$TOKEN = ""  # opcional: Bearer token para servicios protegidos
)

Write-Output "Iniciando prueba E2E - SOLICITUDES: $SOLICITUDES_BASE  RUTAS: $RUTAS_BASE"

$headers = @{}
if ($TOKEN -ne "") {
    $headers.Add("Authorization", "Bearer $TOKEN")
}
$headers.Add("Content-Type", "application/json")

try {
    # 1) Crear solicitud (sin contenedor: proveer peso y volumen)
    $createBody = @{
        direccionOrigen = "Av. Origén 123"
        direccionDestino = "Calle Destino 456"
        clienteEmail = "test@example.com"
        clienteNombre = "Test Cliente"
        clienteTelefono = "+54 9 11 1234 5678"
        contenedorPeso = 1000
        contenedorVolumen = 2
    } | ConvertTo-Json -Depth 5

    Write-Output "1) Creando solicitud..."
    $resp = Invoke-RestMethod -Method Post -Uri "$SOLICITUDES_BASE/api/v1/solicitudes" -Body $createBody -Headers $headers -ErrorAction Stop
    $solicitudId = $resp.id
    Write-Output "=> Solicitud creada. id = $solicitudId"

    # Mostrar estado inicial
    Write-Output "Estado inicial (GET):"
    $s = Invoke-RestMethod -Method Get -Uri "$SOLICITUDES_BASE/api/v1/solicitudes/$solicitudId" -Headers $headers -ErrorAction Stop
    Write-Output ($s | ConvertTo-Json -Depth 5)

    # 2) Solicitar rutas precomputadas a ms-rutas y confirmar una opción -> debe pasar a PROGRAMADA
    Write-Output "2) Solicitando rutas precomputadas a ms-rutas (POST /api/v1/rutas) y confirmando una opción -> debe quedar PROGRAMADA"
    $rutasBody = @{ idSolicitud = $solicitudId } | ConvertTo-Json -Depth 5
    $rutasResp = Invoke-RestMethod -Method Post -Uri "$RUTAS_BASE/api/v1/rutas" -Body $rutasBody -Headers $headers -ErrorAction Stop
    Write-Output "=> Respuesta de ms-rutas: $($rutasResp | ConvertTo-Json -Depth 5)"

    # Intentar extraer una opcionId de la respuesta. Si la respuesta ya contiene un ruta.id usamos PATCH /{id}/ruta
    $opcionId = $null
    if ($rutasResp.opciones -ne $null -and $rutasResp.opciones.Count -gt 0) {
        $opcionId = $rutasResp.opciones[0].id
    } elseif ($rutasResp.id -ne $null) {
        $rutaId = $rutasResp.id
    }

    if ($opcionId -ne $null) {
        Write-Output "Confirmando opcionId $opcionId en ms-solicitudes (POST /{solicitudId}/opciones/{opcionId}/confirmar)"
        $confirmResp = Invoke-RestMethod -Method Post -Uri "$SOLICITUDES_BASE/api/v1/solicitudes/$solicitudId/opciones/$opcionId/confirmar" -Headers $headers -ErrorAction Stop
        Write-Output "=> Confirmación completada. Respuesta: $($confirmResp | ConvertTo-Json -Depth 5)"
        # Obtener estado luego de confirmar
        $after = Invoke-RestMethod -Method Get -Uri "$SOLICITUDES_BASE/api/v1/solicitudes/$solicitudId" -Headers $headers -ErrorAction Stop
        Write-Output "=> Estado tras confirmación: $($after.estado)"
    } elseif ($rutaId -ne $null) {
        Write-Output "Asociando rutaId $rutaId directamente a la solicitud (PATCH /{id}/ruta)"
        $rutaResp = Invoke-RestMethod -Method Patch -Uri "$SOLICITUDES_BASE/api/v1/solicitudes/$solicitudId/ruta?rutaId=$rutaId" -Headers $headers -ErrorAction Stop
        Write-Output "=> Ruta asociada. Estado: $($rutaResp.estado)"
    } else {
        Write-Warning "No se pudo extraer opcionId ni rutaId de la respuesta de ms-rutas. Revise la respuesta arriba."
        exit 1
    }

    # 3) Simular inicio del primer tramo
    Write-Output "3) Simulando inicio del primer tramo (PUT /{id}/estado?nuevoEstado=EN_TRANSITO)"
    $startUri = "$SOLICITUDES_BASE/api/v1/solicitudes/$solicitudId/estado?nuevoEstado=EN_TRANSITO"
    $startResp = Invoke-RestMethod -Method Put -Uri $startUri -Headers $headers -ErrorAction Stop
    Write-Output "=> Estado tras inicio: $($startResp.estado)"

    # 4) Simular finalización del último tramo: marcar COMPLETADA y luego finalizar (persistir costo/tiempo real)
    Write-Output "4) Simulando finalización: cambiar estado a COMPLETADA"
    $finishUri = "$SOLICITUDES_BASE/api/v1/solicitudes/$solicitudId/estado?nuevoEstado=COMPLETADA"
    $finishResp = Invoke-RestMethod -Method Put -Uri $finishUri -Headers $headers -ErrorAction Stop
    Write-Output "=> Estado tras marcar COMPLETADA: $($finishResp.estado)"

    Write-Output "4b) Llamando PATCH /finalizar para persistir costo final y tiempo real"
    $costoFinal = 1600
    $tiempoReal = 9
    $finalizarUri = "$SOLICITUDES_BASE/api/v1/solicitudes/$solicitudId/finalizar?costoFinal=$costoFinal&tiempoReal=$tiempoReal"
    $finalResp = Invoke-RestMethod -Method Patch -Uri $finalizarUri -Headers $headers -ErrorAction Stop
    Write-Output "=> Finalizado: estado = $($finalResp.estado) costoFinal = $($finalResp.costoFinal) tiempoReal = $($finalResp.tiempoReal)"

    # Verificación final: obtener la solicitud y mostrar estado
    Write-Output "Verificación final (GET):"
    $finalGet = Invoke-RestMethod -Method Get -Uri "$SOLICITUDES_BASE/api/v1/solicitudes/$solicitudId" -Headers $headers -ErrorAction Stop
    Write-Output ($finalGet | ConvertTo-Json -Depth 6)

    Write-Output "E2E script finished successfully. Revise los estados en la respuesta final arriba."
} catch {
    Write-Error "Error durante la ejecución: $($_.Exception.Message)"
    if ($_.Exception.Response -ne $null) {
        $stream = $_.Exception.Response.GetResponseStream()
        if ($stream -ne $null) {
            $reader = New-Object System.IO.StreamReader($stream)
            $body = $reader.ReadToEnd()
            Write-Output "Response body: $body"
        }
    }
    exit 1
}
