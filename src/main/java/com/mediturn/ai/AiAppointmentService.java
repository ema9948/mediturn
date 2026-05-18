package com.mediturn.ai;

import org.springframework.stereotype.Service;

/**
 * Servicio de IA para sugerencia de turnos por lenguaje natural.
 * Integración con Claude API — Fase 2.
 */
@Service
public class AiAppointmentService {

    // TODO Fase 2: integrar Claude API
    // - Recibir texto libre del paciente ("quiero un turno con cardiología el viernes")
    // - Extraer intención, especialidad y preferencia horaria
    // - Consultar disponibilidad y retornar sugerencia estructurada

    public String suggest(String userMessage) {
        throw new UnsupportedOperationException("AI integration coming in Phase 2");
    }
}
