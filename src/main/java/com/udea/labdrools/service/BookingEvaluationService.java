package com.udea.labdrools.service;

import com.udea.labdrools.model.BookingRequest;
import com.udea.labdrools.model.BookingResponse;
import com.udea.labdrools.model.Seat;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de evaluación de solicitudes de vuelo (lógica de negocio).
 *
 * Toda la lógica de negocio vive en las reglas de
 * src/main/resources/rules/airline_rules.drl; este servicio se encarga de
 * orquestar la sesión de Drools: crearla, insertar los hechos, disparar las
 * reglas y liberar la sesión.
 */
@Service
public class BookingEvaluationService {

    @Autowired
    private KieContainer kieContainer;

    /**
     * Evalúa la solicitud con los asientos disponibles indicados.
     *
     * Los hechos Seat son necesarios para que se disparen las reglas
     * AssignEmergencyExitSeatToYoungAdults (salida de emergencia) y
     * PreferentialSeatForFamilies (asiento preferencial para familias);
     * una vez asignados, el servicio los marca como ocupados en la respuesta
     * a través de las propias reglas.
     *
     * @param request        solicitud de vuelo (pasajero, vuelo, equipaje, precio)
     * @param availableSeats asientos libres insertados en la sesión
     * @return respuesta con los mensajes de las reglas disparadas y el descuento/precio final
     */
    public BookingResponse evaluateBooking(BookingRequest request, List<Seat> availableSeats) {
        // Crear una respuesta inicial
        BookingResponse response = new BookingResponse();

        // Crear una sesión en Drools
        KieSession kieSession = kieContainer.newKieSession();
        try {
            // Insertar los hechos (request, response y asientos) en la sesión
            kieSession.insert(request);
            kieSession.insert(response);
            if (availableSeats != null) {
                for (Seat seat : availableSeats) {
                    kieSession.insert(seat);
                }
            }
            // Ejecutar todas las reglas
            kieSession.fireAllRules();
        } finally {
            // Liberar la sesión
            kieSession.dispose();
        }
        return response;
    }

    /**
     * Evalúa la solicitud sin hechos de asiento.
     *
     * Útil cuando solo interesan las reglas de pasajero, vuelo y equipaje;
     * las reglas 5 (salida de emergencia) y 10 (asiento para familias) no se
     * disparan porque necesitan un hecho Seat disponible en la sesión.
     */
    public BookingResponse evaluateBooking(BookingRequest request) {
        return evaluateBooking(request, List.of());
    }
}
