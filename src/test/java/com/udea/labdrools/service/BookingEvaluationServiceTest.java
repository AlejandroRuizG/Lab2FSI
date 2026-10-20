package com.udea.labdrools.service;

import com.udea.labdrools.model.BookingRequest;
import com.udea.labdrools.model.BookingResponse;
import com.udea.labdrools.model.Flight;
import com.udea.labdrools.model.Luggage;
import com.udea.labdrools.model.Passenger;
import com.udea.labdrools.model.Seat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba el servicio de evaluación con el KieContainer inyectado por Spring.
 */
@SpringBootTest
class BookingEvaluationServiceTest {

    @Autowired
    private BookingEvaluationService bookingEvaluationService;

    @Test
    void evaluatesPassengerFlightAndLuggageRules() {
        Passenger passenger = new Passenger("María Gómez", 70, "Platinum", "Window", false);
        Flight flight = new Flight("XX001", 200, 6.0);
        Luggage luggage = new Luggage("TAG-001", 8);
        BookingRequest request = new BookingRequest(passenger, flight, luggage, 500.0);

        BookingResponse response = bookingEvaluationService.evaluateBooking(request);

        // Regla 1: Platinum + retraso de 200 min > 60
        assertTrue(passenger.isUpgradedToBusinessClass());
        // Regla 2: mayor de 65 años
        assertTrue(passenger.isPriorityCheckInEnabled());
        // Regla 3: equipaje de 8 kg < 10
        assertEquals(10.0, response.getDiscountPercent());
        assertEquals(450.0, response.getFinalPrice());
        // Regla 6: retraso de 200 min > 180
        assertEquals(200.0, passenger.getCompensationAmount());
        // Regla 7: membresía distinta a Basic en vuelo de 6 h
        assertEquals(500, passenger.getLoyaltyPoints());
        // Regla 9: miembro Platinum
        assertTrue(passenger.isVipLoungeAccess());
        // Sin hechos Seat no se asignan asientos (reglas 5 y 10)
        assertNull(passenger.getAssignedSeat());
        // La traza de reglas quedó en la respuesta
        assertFalse(response.getMessages().isEmpty());
    }

    @Test
    void assignsEmergencyExitSeatWhenSeatsAreAvailable() {
        Passenger passenger = new Passenger("Joven Test", 30, "Basic", "Any", false);
        Flight flight = new Flight("XX002", 0, 3.0);
        Luggage luggage = new Luggage("TAG-002", 20);
        BookingRequest request = new BookingRequest(passenger, flight, luggage, 300.0);
        Seat emergencySeat = new Seat("12A", Seat.EMERGENCY_EXIT);

        BookingResponse response = bookingEvaluationService.evaluateBooking(
                request, List.of(emergencySeat));

        // Regla 5: 18-40 años, preferencia "Any" y asiento de salida libre
        assertEquals("12A", passenger.getAssignedSeat());
        assertTrue(emergencySeat.isOccupied());
        assertFalse(response.getMessages().isEmpty());
    }

    @Test
    void doesNotAssignSeatWhenPassengerHasSpecificPreference() {
        Passenger passenger = new Passenger("Ventana Test", 30, "Basic", "Window", false);
        Flight flight = new Flight("XX003", 0, 3.0);
        Luggage luggage = new Luggage("TAG-003", 20);
        BookingRequest request = new BookingRequest(passenger, flight, luggage, 300.0);
        Seat emergencySeat = new Seat("12B", Seat.EMERGENCY_EXIT);

        bookingEvaluationService.evaluateBooking(request, List.of(emergencySeat));

        assertNull(passenger.getAssignedSeat());
        assertFalse(emergencySeat.isOccupied());
    }
}
