package com.udea.labdrools.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Equipaje del pasajero.
 *
 * - weightKg: peso en kilogramos.
 * - allowed: false cuando una regla marca el equipaje como no permitido.
 */
public class Luggage {

    @NotBlank(message = "La etiqueta del equipaje no puede estar vacía")
    @Size(max = 30, message = "La etiqueta del equipaje no puede exceder 30 caracteres")
    private String tag;

    @DecimalMin(value = "0.0", message = "El peso del equipaje no puede ser negativo")
    private double weightKg;

    // Resultado: false cuando la regla RestrictLuggageOnShortFlights lo marca como no permitido
    private boolean allowed = true;

    public Luggage() {
    }

    public Luggage(String tag, double weightKg) {
        this.tag = tag;
        this.weightKg = weightKg;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
}
