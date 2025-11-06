package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.CostoRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.CostoResponseDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaResponseDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.SolicitudIntegrationDTO;
import com.backend.tpi.ms_gestion_calculos.models.Tarifa;
import com.backend.tpi.ms_gestion_calculos.models.TarifaVolumenPeso;
import com.backend.tpi.ms_gestion_calculos.repositories.TarifaRepository;
import com.backend.tpi.ms_gestion_calculos.repositories.TarifaVolumenPesoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PrecioService {

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TarifaVolumenPesoRepository tarifaVolumenPesoRepository;

    @Autowired
    private CalculoService calculoService;

    @Autowired
    private RestClient solicitudesClient;

    /**
     * Calcula un costo estimado en base a distancia, tarifas y una tabla por volumen/peso.
     * Algoritmo (simple): costo = costoBaseGestionFijo + precioPorKm * distancia + cargoPorVolumenPeso
     */
    public CostoResponseDTO calcularCostoEstimado(CostoRequestDTO request) {
        // obtener distancia usando el servicio de calculos
        DistanciaRequestDTO distanciaReq = new DistanciaRequestDTO();
        distanciaReq.setOrigen(request.getOrigen());
        distanciaReq.setDestino(request.getDestino());
        DistanciaResponseDTO distanciaResp = calculoService.calcularDistancia(distanciaReq);
        double distancia = distanciaResp != null && distanciaResp.getDistancia() != null ? distanciaResp.getDistancia() : 0.0;

        // obtener tarifa base (la más reciente si existe)
        Tarifa tarifa = tarifaRepository.findTopByOrderByIdDesc();
        double costoBase = tarifa != null && tarifa.getCostoBaseGestionFijo() != null ? tarifa.getCostoBaseGestionFijo().doubleValue() : 0.0;
        double precioPorKm = tarifa != null && tarifa.getValorLitroCombustible() != null ? tarifa.getValorLitroCombustible().doubleValue() : 1.0;

        // buscar cargo por volumen/peso aplicable
        List<TarifaVolumenPeso> tvps = tarifaVolumenPesoRepository.findAll();
        double cargoVolumenPeso = 0.0;
        if (tvps != null && !tvps.isEmpty()) {
            for (TarifaVolumenPeso t : tvps) {
                boolean aplicaPeso = request.getPeso() == null || t.getPesoMax() == null || request.getPeso() <= t.getPesoMax();
                boolean aplicaVolumen = request.getVolumen() == null || t.getVolumenMax() == null || request.getVolumen() <= t.getVolumenMax();
                if (aplicaPeso && aplicaVolumen) {
                    cargoVolumenPeso = t.getPrecio() != null ? t.getPrecio() : 0.0;
                    break;
                }
            }
        }

        double costo = costoBase + (precioPorKm * distancia) + cargoVolumenPeso;

        // redondear a 2 decimales
        BigDecimal bd = BigDecimal.valueOf(costo).setScale(2, RoundingMode.HALF_UP);

        // estimar tiempo: velocidad promedio 60 km/h
        String tiempoEstimado = "N/A";
        if (distancia > 0) {
            double horas = distancia / 60.0;
            int h = (int) horas;
            int minutos = (int) Math.round((horas - h) * 60);
            tiempoEstimado = String.format("%dh %02dm", h, minutos);
        }

        CostoResponseDTO resp = new CostoResponseDTO();
        resp.setCostoTotal(bd.doubleValue());
        resp.setTiempoEstimado(tiempoEstimado);
        return resp;
    }

    /**
     * Integra con ms-solicitudes para obtener los datos de la solicitud y calcular el costo.
     * Si falla la comunicación, cae a un cálculo por defecto como fallback.
     */
    public CostoResponseDTO calcularCostoParaSolicitud(Long solicitudId) {
        try {
        ResponseEntity<SolicitudIntegrationDTO> solicitudEntity = solicitudesClient.get()
                .uri("/api/v1/solicitudes/{id}", solicitudId)
                .retrieve()
                .toEntity(SolicitudIntegrationDTO.class);

            SolicitudIntegrationDTO solicitud = solicitudEntity != null ? solicitudEntity.getBody() : null;
            if (solicitud != null) {
                CostoRequestDTO req = new CostoRequestDTO();
                req.setOrigen(solicitud.getDireccionOrigen());
                req.setDestino(solicitud.getDireccionDestino());
                // ms-solicitudes no expone peso/volumen por ahora -> usar valores por defecto
                req.setPeso(1000.0);
                req.setVolumen(10.0);
                CostoResponseDTO resp = calcularCostoEstimado(req);
                resp.setTiempoEstimado(resp.getTiempoEstimado() + " (calculado desde ms-solicitudes)");
                return resp;
            }
        } catch (Exception ex) {
            // Loguear en un caso real; por ahora caemos al fallback
        }

        // Fallback: estimación por defecto
        CostoRequestDTO fallback = new CostoRequestDTO();
        fallback.setOrigen("Buenos Aires");
        fallback.setDestino("Rosario");
        fallback.setPeso(1000.0);
        fallback.setVolumen(10.0);
        CostoResponseDTO resp = calcularCostoEstimado(fallback);
        resp.setTiempoEstimado(resp.getTiempoEstimado() + " (estimado - fallback)");
        return resp;
    }
}
