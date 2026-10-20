package com.udea.labdrools.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Vuelo al que se asocia la solicitud.
 *
 * - delayMinutes: minutos de retraso del vuelo.
 * - durationHours: duración total del vuelo en horas.
 */
public class Flight {

    @NotBlank(message = "El número de vuelo no puede estar vacío")
    @Size(min = 2, max = 10, message = "El número de vuelo debe tener entre 2 y 10 caracteres")
    private String flightNumber;

    @Min(value = 0, message = "El retraso no puede ser negativo")
    private int delayMinutes;

    @DecimalMin(value = "0.0", inclusive = false,
            message = "La duración del vuelo debe ser mayor a 0 horas")
    private double durationHours;

    public Flight() {
    }

    public Flight(String flightNumber, int delayMinutes, double durationHours) {
        this.flightNumber = flightNumber;
        this.delayMinutes = delayMinutes;
        this.durationHours = durationHours;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(int delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(double durationHours) {
        this.durationHours = durationHours;
    }
}
