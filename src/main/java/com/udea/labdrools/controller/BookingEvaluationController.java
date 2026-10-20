package com.udea.labdrools.controller;

import com.udea.labdrools.ai.AIExplanationService;
import com.udea.labdrools.model.BookingRequest;
import com.udea.labdrools.model.BookingResponse;
import com.udea.labdrools.service.BookingEvaluationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de evaluación de solicitudes de vuelo.
 *
 * Endpoints:
 * - POST /booking/api/evaluate       -> evaluación con Drools (con @Valid)
 * - POST /booking/evaluateBooking    -> evaluación con Drools + explicación con IA
 */
@RestController
@RequestMapping("/booking")
public class BookingEvaluationController {

    @Autowired
    private BookingEvaluationService evaluationService;

    @Autowired
    private AIExplanationService aiService;

    // Endpoint REST: evaluación de la solicitud con validación de los datos de entrada
    @PostMapping("/api/evaluate")
    public BookingResponse evaluateBookingApi(@Valid @RequestBody BookingRequest request,
                                              BindingResult result) {
        if (result.hasErrors()) {
            // Concatenar todos los mensajes de validación en uno solo
            String errorMessage = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Errores de validación");
            BookingResponse response = new BookingResponse();
            response.addMessage("Error de validación: " + errorMessage);
            return response;
        }
        return evaluationService.evaluateBooking(request, request.getAvailableSeats());
    }

    // Endpoint REST: evaluación de la solicitud + explicación con IA de la decisión
    @PostMapping("/evaluateBooking")
    public EvaluationWithAiResponse evaluateBookingWithAi(@RequestBody BookingRequest request) {
        BookingResponse decision = evaluationService.evaluateBooking(
                request, request.getAvailableSeats());
        String explanation = aiService.explain(String.join(" ", decision.getMessages()));
        return new EvaluationWithAiResponse(decision, explanation);
    }

    /**
     * Respuesta del endpoint con IA:
     * { "result": { ...BookingResponse... }, "aiExplanation": "..." }
     */
    public record EvaluationWithAiResponse(BookingResponse result, String aiExplanation) {
    }
}
