package com.udea.labdrools.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Pasajero de la aerolínea.
 *
 * Valores soportados:
 * - membershipStatus: "Basic", "Gold", "Platinum"
 * - seatPreference: "Any" (sin preferencia), "Window", "Aisle"
 *
 * Los atributos que inician en false/true por defecto son resultados
 * que las reglas de negocio actualizan (ascenso, check-in prioritario, etc.).
 */
public class Passenger {

    // ---- Datos de entrada ----
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @Min(value = 0, message = "La edad no puede ser negativa")
    @Max(value = 120, message = "La edad no puede superar los 120 años")
    private int age;

    @NotBlank(message = "El estatus de membresía no puede estar vacío")
    @Pattern(regexp = "Basic|Gold|Platinum",
            message = "El estatus de membresía debe ser Basic, Gold o Platinum")
    private String membershipStatus;

    // Null = sin preferencia de asiento (lo contempla la regla 10), por eso no lleva @NotNull
    @Size(max = 20, message = "La preferencia de asiento no puede exceder 20 caracteres")
    private String seatPreference;

    private boolean travelingWithChildren;

    // ---- Resultados de la evaluación de reglas ----
    private boolean upgradedToBusinessClass = false;
    private boolean priorityCheckInEnabled = false;
    private boolean eligibleForUpgrade = true;
    private boolean vipLoungeAccess = false;

    // Null = ningún asiento asignado todavía, por eso no lleva @NotNull
    private String assignedSeat;

    @DecimalMin(value = "0.0", message = "La compensación no puede ser negativa")
    private double compensationAmount = 0.0;

    @Min(value = 0, message = "Los puntos de lealtad no pueden ser negativos")
    private int loyaltyPoints = 0;

    public Passenger() {
    }

    public Passenger(String name, int age, String membershipStatus, String seatPreference,
                     boolean travelingWithChildren) {
        this.name = name;
        this.age = age;
        this.membershipStatus = membershipStatus;
        this.seatPreference = seatPreference;
        this.travelingWithChildren = travelingWithChildren;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public String getSeatPreference() {
        return seatPreference;
    }

    public void setSeatPreference(String seatPreference) {
        this.seatPreference = seatPreference;
    }

    public boolean isTravelingWithChildren() {
        return travelingWithChildren;
    }

    public void setTravelingWithChildren(boolean travelingWithChildren) {
        this.travelingWithChildren = travelingWithChildren;
    }

    public boolean isUpgradedToBusinessClass() {
        return upgradedToBusinessClass;
    }

    public void setUpgradedToBusinessClass(boolean upgradedToBusinessClass) {
        this.upgradedToBusinessClass = upgradedToBusinessClass;
    }

    public boolean isPriorityCheckInEnabled() {
        return priorityCheckInEnabled;
    }

    public void setPriorityCheckInEnabled(boolean priorityCheckInEnabled) {
        this.priorityCheckInEnabled = priorityCheckInEnabled;
    }

    public boolean isEligibleForUpgrade() {
        return eligibleForUpgrade;
    }

    public void setEligibleForUpgrade(boolean eligibleForUpgrade) {
        this.eligibleForUpgrade = eligibleForUpgrade;
    }

    public boolean isVipLoungeAccess() {
        return vipLoungeAccess;
    }

    public void setVipLoungeAccess(boolean vipLoungeAccess) {
        this.vipLoungeAccess = vipLoungeAccess;
    }

    public String getAssignedSeat() {
        return assignedSeat;
    }

    public void setAssignedSeat(String assignedSeat) {
        this.assignedSeat = assignedSeat;
    }

    public double getCompensationAmount() {
        return compensationAmount;
    }

    public void setCompensationAmount(double compensationAmount) {
        this.compensationAmount = compensationAmount;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
}
