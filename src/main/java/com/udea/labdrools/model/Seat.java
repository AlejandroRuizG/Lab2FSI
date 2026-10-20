package com.udea.labdrools.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Asiento del avión.
 *
 * Valores soportados para seatType:
 * - "EMERGENCY_EXIT": asiento de salida de emergencia.
 * - "FAMILY": asiento preferencial para familias.
 * - "STANDARD": asiento estándar.
 *
 * occupied se vuelve true cuando una regla asigna el asiento.
 */
public class Seat {

    public static final String EMERGENCY_EXIT = "EMERGENCY_EXIT";
    public static final String FAMILY = "FAMILY";
    public static final String STANDARD = "STANDARD";

    @NotBlank(message = "El número de asiento no puede estar vacío")
    @Size(max = 5, message = "El número de asiento no puede exceder 5 caracteres")
    private String seatNumber;

    @NotBlank(message = "El tipo de asiento no puede estar vacío")
    @Pattern(regexp = "EMERGENCY_EXIT|FAMILY|STANDARD",
            message = "El tipo de asiento debe ser EMERGENCY_EXIT, FAMILY o STANDARD")
    private String seatType;

    // Resultado: true cuando una regla asigna el asiento
    private boolean occupied = false;

    public Seat() {
    }

    public Seat(String seatNumber, String seatType) {
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}
