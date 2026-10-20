package com.udea.labdrools.controller;

import com.udea.labdrools.ai.AIExplanationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba los endpoints del BookingEvaluationController con MockMvc.
 * La explicación con IA se reemplaza por un MockBean para no llamar a OpenAI.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BookingEvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIExplanationService aiExplanationService;

    private static final String VALID_REQUEST = """
            {
              "passenger": {
                "name": "María Gómez",
                "age": 70,
                "membershipStatus": "Platinum",
                "seatPreference": "Any",
                "travelingWithChildren": false
              },
              "flight": { "flightNumber": "XX001", "delayMinutes": 200, "durationHours": 6.0 },
              "luggage": { "tag": "TAG-001", "weightKg": 8 },
              "ticketPrice": 500.0,
              "availableSeats": [
                { "seatNumber": "12A", "seatType": "EMERGENCY_EXIT" },
                { "seatNumber": "F1", "seatType": "FAMILY" }
              ]
            }
            """;

    @Test
    void evaluateApiRunsRulesOnValidRequest() throws Exception {
        mockMvc.perform(post("/booking/api/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0]").exists())
                .andExpect(jsonPath("$.discountPercent").value(10.0))
                .andExpect(jsonPath("$.finalPrice").value(450.0));
    }

    @Test
    void evaluateApiReturnsValidationMessagesOnInvalidRequest() throws Exception {
        String invalidRequest = """
                {
                  "passenger": {
                    "name": " ",
                    "age": 30,
                    "membershipStatus": "Gold",
                    "seatPreference": "Window",
                    "travelingWithChildren": false
                  },
                  "flight": { "flightNumber": "XX", "delayMinutes": 0, "durationHours": 1.0 },
                  "luggage": { "tag": "TAG-009", "weightKg": 10 },
                  "ticketPrice": -100
                }
                """;

        mockMvc.perform(post("/booking/api/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0]").value(
                        containsString("Error de validación")))
                .andExpect(jsonPath("$.messages[0]").value(
                        containsString("El nombre no puede estar vacío")))
                .andExpect(jsonPath("$.messages[0]").value(
                        containsString("El precio del boleto no puede ser negativo")));
    }

    @Test
    void evaluateBookingWithAiReturnsResultAndExplanation() throws Exception {
        Mockito.when(aiExplanationService.explain(anyString()))
                .thenReturn("Explicación de prueba");

        String youngAdultRequest = """
                {
                  "passenger": {
                    "name": "Joven Test",
                    "age": 30,
                    "membershipStatus": "Basic",
                    "seatPreference": "Any",
                    "travelingWithChildren": false
                  },
                  "flight": { "flightNumber": "XX002", "delayMinutes": 0, "durationHours": 3.0 },
                  "luggage": { "tag": "TAG-010", "weightKg": 20 },
                  "ticketPrice": 300.0,
                  "availableSeats": [
                    { "seatNumber": "12A", "seatType": "EMERGENCY_EXIT" }
                  ]
                }
                """;

        mockMvc.perform(post("/booking/evaluateBooking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(youngAdultRequest))
                .andExpect(status().isOk())
                // Regla 5: pasajero 18-40 con preferencia "Any" y salida de emergencia libre
                .andExpect(jsonPath("$.result.messages[0]").value(
                        containsString("Asiento de salida de emergencia 12A")))
                .andExpect(jsonPath("$.aiExplanation").value("Explicación de prueba"));
    }

    @Test
    void evaluateBookingWithAiUsesFallbackWhenNoMessages() throws Exception {
        Mockito.when(aiExplanationService.explain(anyString()))
                .thenReturn("No hay una decisión para explicar.");

        String requestWithoutRules = """
                {
                  "passenger": {
                    "name": "Pasajero Neutro",
                    "age": 30,
                    "membershipStatus": "Basic",
                    "seatPreference": "Window",
                    "travelingWithChildren": false
                  },
                  "flight": { "flightNumber": "XX003", "delayMinutes": 0, "durationHours": 1.0 },
                  "luggage": { "tag": "TAG-011", "weightKg": 15 },
                  "ticketPrice": 200.0
                }
                """;

        mockMvc.perform(post("/booking/evaluateBooking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithoutRules))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.messages").isEmpty())
                .andExpect(jsonPath("$.aiExplanation")
                        .value("No hay una decisión para explicar."));
    }
}
