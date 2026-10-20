package com.udea.labdrools.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Respuesta de la evaluación de las reglas de negocio.
 *
 * - messages: traza de todas las acciones disparadas por las reglas.
 * - discountPercent: descuento aplicado sobre el boleto (0 = sin descuento).
 * - finalPrice: precio del boleto con el descuento aplicado. Solo se calcula
 *   cuando una regla otorga descuento; si discountPercent es 0, aplica el
 *   precio original (ticketPrice del BookingRequest).
 */
public class BookingResponse {

    private final List<String> messages = new ArrayList<>();
    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El descuento no puede superar el 100%")
    private double discountPercent = 0.0;

    @DecimalMin(value = "0.0", message = "El precio final no puede ser negativo")
    private double finalPrice = 0.0;

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
