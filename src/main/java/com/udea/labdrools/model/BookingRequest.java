package com.udea.labdrools.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Solicitud de vuelo: hechos de entrada que consumen las reglas.
 *
 * - passenger: pasajero y resultados de la evaluación.
 * - flight: datos del vuelo (retraso y duración).
 * - luggage: equipaje del pasajero.
 * - ticketPrice: precio original del boleto.
 * - availableSeats: asientos libres insertados en la sesión de Drools para
 *   que participen las reglas de asiento (salida de emergencia y familias).
 */
public class BookingRequest {

    @Valid
    @NotNull(message = "El pasajero es obligatorio")
    private Passenger passenger;

    @Valid
    @NotNull(message = "El vuelo es obligatorio")
    private Flight flight;

    @Valid
    @NotNull(message = "El equipaje es obligatorio")
    private Luggage luggage;

    @DecimalMin(value = "0.0", message = "El precio del boleto no puede ser negativo")
    private double ticketPrice;

    // Opcional: si no se envía, queda como lista vacía y las reglas 5 y 10 no participan
    @Valid
    private List<Seat> availableSeats = new ArrayList<>();

    public BookingRequest() {
    }

    public BookingRequest(Passenger passenger, Flight flight, Luggage luggage, double ticketPrice) {
        this.passenger = passenger;
        this.flight = flight;
        this.luggage = luggage;
        this.ticketPrice = ticketPrice;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public Luggage getLuggage() {
        return luggage;
    }

    public void setLuggage(Luggage luggage) {
        this.luggage = luggage;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public List<Seat> getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(List<Seat> availableSeats) {
        this.availableSeats = availableSeats;
    }
}
