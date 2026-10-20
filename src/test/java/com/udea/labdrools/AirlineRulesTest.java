package com.udea.labdrools;

import com.udea.labdrools.model.BookingRequest;
import com.udea.labdrools.model.BookingResponse;
import com.udea.labdrools.model.Flight;
import com.udea.labdrools.model.Luggage;
import com.udea.labdrools.model.Passenger;
import com.udea.labdrools.model.Seat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de las 10 reglas de negocio definidas en
 * src/main/resources/rules/airline_rules.drl
 */
class AirlineRulesTest {

    private static KieContainer kieContainer;

    @BeforeAll
    static void compileRules() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(kieServices.getResources()
                .newClassPathResource("rules/airline_rules.drl"));

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        assertFalse(kieBuilder.getResults().hasMessages(Message.Level.ERROR),
                () -> "Errores al compilar las reglas: " + kieBuilder.getResults().getMessages());

        kieContainer = kieServices.newKieContainer(
                kieServices.getRepository().getDefaultReleaseId());
    }

    // ---------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------

    private record Evaluation(BookingRequest request, BookingResponse response) {
    }

    /** Datos neutros: ninguna regla debería dispararse. */
    private static Passenger neutralPassenger() {
        return new Passenger("Pasajero Test", 30, "Basic", "Window", false);
    }

    private static Flight neutralFlight() {
        return new Flight("XX001", 0, 1.0);
    }

    private static Luggage neutralLuggage() {
        return new Luggage("TAG-001", 15);
    }

    private Evaluation evaluate(Passenger passenger, Flight flight, Luggage luggage,
                                Seat... seats) {
        BookingRequest request = new BookingRequest(passenger, flight, luggage, 500.0);
        BookingResponse response = new BookingResponse();

        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(request);
            session.insert(response);
            for (Seat seat : seats) {
                session.insert(seat);
            }
            session.fireAllRules();
        } finally {
            session.dispose();
        }
        return new Evaluation(request, response);
    }

    private Evaluation evaluate(Passenger passenger) {
        return evaluate(passenger, neutralFlight(), neutralLuggage());
    }

    // ---------------------------------------------------------
    // Regla 1: UpgradeToBusinessClassForFrequentFlyersWithDelays
    // ---------------------------------------------------------

    @Test
    void upgradeToBusinessClassForGoldWithLongDelay() {
        Passenger passenger = neutralPassenger();
        passenger.setMembershipStatus("Gold");

        Evaluation result = evaluate(passenger,
                new Flight("XX001", 90, 1.0), neutralLuggage());

        assertTrue(result.request().getPassenger().isUpgradedToBusinessClass());
        assertFalse(result.response().getMessages().isEmpty());
    }

    @Test
    void upgradeToBusinessClassForPlatinumWithLongDelay() {
        Passenger passenger = neutralPassenger();
        passenger.setMembershipStatus("Platinum");

        Evaluation result = evaluate(passenger,
                new Flight("XX001", 61, 1.0), neutralLuggage());

        assertTrue(result.request().getPassenger().isUpgradedToBusinessClass());
    }

    @Test
    void noUpgradeWithoutEnoughDelayOrStatus() {
        Passenger gold = neutralPassenger();
        gold.setMembershipStatus("Gold");
        Evaluation shortDelay = evaluate(gold, new Flight("XX001", 30, 1.0), neutralLuggage());
        assertFalse(shortDelay.request().getPassenger().isUpgradedToBusinessClass());

        Passenger basic = neutralPassenger();
        Evaluation basicStatus = evaluate(basic, new Flight("XX001", 90, 1.0), neutralLuggage());
        assertFalse(basicStatus.request().getPassenger().isUpgradedToBusinessClass());
    }

    // ---------------------------------------------------------
    // Regla 2: PriorityCheckInForSeniors
    // ---------------------------------------------------------

    @Test
    void priorityCheckInForSeniorsOlderThan65() {
        Passenger senior = neutralPassenger();
        senior.setAge(70);

        Evaluation result = evaluate(senior);

        assertTrue(result.request().getPassenger().isPriorityCheckInEnabled());
    }

    @Test
    void noPriorityCheckInFor65OrYounger() {
        Passenger exactly65 = neutralPassenger();
        exactly65.setAge(65);

        Evaluation result = evaluate(exactly65);

        assertFalse(result.request().getPassenger().isPriorityCheckInEnabled());
    }

    // ---------------------------------------------------------
    // Regla 3: DiscountForLightLuggage
    // ---------------------------------------------------------

    @Test
    void tenPercentDiscountForLightLuggage() {
        Passenger passenger = neutralPassenger();

        Evaluation result = evaluate(passenger, neutralFlight(), new Luggage("TAG-002", 8));

        assertEquals(10.0, result.response().getDiscountPercent());
        assertEquals(450.0, result.response().getFinalPrice());
    }

    @Test
    void noDiscountForLuggageOfTenKgOrMore() {
        Passenger passenger = neutralPassenger();

        Evaluation result = evaluate(passenger, neutralFlight(), new Luggage("TAG-003", 10));

        assertEquals(0.0, result.response().getDiscountPercent());
    }

    // ---------------------------------------------------------
    // Regla 4: DenyUpgradeForOverweightLuggage
    // ---------------------------------------------------------

    @Test
    void denyUpgradeForOverweightLuggage() {
        Passenger passenger = neutralPassenger();

        Evaluation result = evaluate(passenger, neutralFlight(), new Luggage("TAG-004", 25));

        assertFalse(result.request().getPassenger().isEligibleForUpgrade());
    }

    @Test
    void upgradeAllowedUpTo23kg() {
        Passenger passenger = neutralPassenger();

        Evaluation result = evaluate(passenger, neutralFlight(), new Luggage("TAG-005", 23));

        assertTrue(result.request().getPassenger().isEligibleForUpgrade());
    }

    // ---------------------------------------------------------
    // Regla 5: AssignEmergencyExitSeatToYoungAdults
    // ---------------------------------------------------------

    @Test
    void assignEmergencyExitSeatToYoungAdultWithAnyPreference() {
        Passenger passenger = new Passenger("Joven Test", 30, "Basic", "Any", false);
        Seat emergencySeat = new Seat("12A", Seat.EMERGENCY_EXIT);

        Evaluation result = evaluate(passenger, neutralFlight(), neutralLuggage(), emergencySeat);

        assertEquals("12A", result.request().getPassenger().getAssignedSeat());
        assertTrue(emergencySeat.isOccupied());
    }

    @Test
    void noEmergencySeatAssignmentWhenSeatIsOccupiedOrPreferenceIsNotAny() {
        Seat occupiedSeat = new Seat("12A", Seat.EMERGENCY_EXIT);
        occupiedSeat.setOccupied(true);

        Passenger youngWithAny = new Passenger("Joven Test", 30, "Basic", "Any", false);
        evaluate(youngWithAny, neutralFlight(), neutralLuggage(), occupiedSeat);
        assertNull(youngWithAny.getAssignedSeat());
        assertTrue(occupiedSeat.isOccupied());

        Passenger withWindowPreference = new Passenger("Joven Test", 30, "Basic", "Window", false);
        Seat freeSeat = new Seat("12B", Seat.EMERGENCY_EXIT);
        evaluate(withWindowPreference, neutralFlight(), neutralLuggage(), freeSeat);
        assertNull(withWindowPreference.getAssignedSeat());
        assertFalse(freeSeat.isOccupied());
    }

    // ---------------------------------------------------------
    // Regla 6: CompensationForExtremeDelays
    // ---------------------------------------------------------

    @Test
    void compensationOf200ForExtremeDelay() {
        Passenger passenger = neutralPassenger();

        Evaluation result = evaluate(passenger, new Flight("XX001", 200, 1.0), neutralLuggage());

        assertEquals(200.0, result.request().getPassenger().getCompensationAmount());
    }

    @Test
    void noCompensationForDelayOf180MinutesOrLess() {
        Passenger passenger = neutralPassenger();

        Evaluation result = evaluate(passenger, new Flight("XX001", 180, 1.0), neutralLuggage());

        assertEquals(0.0, result.request().getPassenger().getCompensationAmount());
    }

    // ---------------------------------------------------------
    // Regla 7: ExtraLoyaltyPointsForLongFlights
    // ---------------------------------------------------------

    @Test
    void extra500LoyaltyPointsForNonBasicMembersOnLongFlights() {
        Passenger passenger = neutralPassenger();
        passenger.setMembershipStatus("Gold");
        passenger.setLoyaltyPoints(1000);

        Evaluation result = evaluate(passenger, new Flight("XX001", 0, 6.0), neutralLuggage());

        assertEquals(1500, result.request().getPassenger().getLoyaltyPoints());
    }

    @Test
    void noExtraPointsForBasicMembersOrShortFlights() {
        Passenger basicOnLongFlight = neutralPassenger();
        basicOnLongFlight.setLoyaltyPoints(1000);
        evaluate(basicOnLongFlight, new Flight("XX001", 0, 6.0), neutralLuggage());
        assertEquals(1000, basicOnLongFlight.getLoyaltyPoints());

        Passenger goldOnShortFlight = neutralPassenger();
        goldOnShortFlight.setMembershipStatus("Gold");
        goldOnShortFlight.setLoyaltyPoints(1000);
        evaluate(goldOnShortFlight, new Flight("XX001", 0, 5.0), neutralLuggage());
        assertEquals(1000, goldOnShortFlight.getLoyaltyPoints());
    }

    // ---------------------------------------------------------
    // Regla 8: RestrictLuggageOnShortFlights
    // ---------------------------------------------------------

    @Test
    void restrictHeavyLuggageOnShortFlights() {
        Passenger passenger = neutralPassenger();
        Luggage luggage = new Luggage("TAG-006", 20);

        Evaluation result = evaluate(passenger, new Flight("XX001", 0, 1.5), luggage);

        assertFalse(result.request().getLuggage().isAllowed());
    }

    @Test
    void heavyLuggageAllowedOnLongerFlights() {
        Passenger passenger = neutralPassenger();
        Luggage luggage = new Luggage("TAG-007", 20);

        Evaluation result = evaluate(passenger, new Flight("XX001", 0, 3.0), luggage);

        assertTrue(result.request().getLuggage().isAllowed());
    }

    // ---------------------------------------------------------
    // Regla 9: VipLoungeAccessForPlatinumMembers
    // ---------------------------------------------------------

    @Test
    void vipLoungeAccessForPlatinumMembers() {
        Passenger platinum = neutralPassenger();
        platinum.setMembershipStatus("Platinum");

        Evaluation result = evaluate(platinum);

        assertTrue(result.request().getPassenger().isVipLoungeAccess());
    }

    @Test
    void noVipLoungeAccessForOtherMembers() {
        Passenger gold = neutralPassenger();
        gold.setMembershipStatus("Gold");

        Evaluation result = evaluate(gold);

        assertFalse(result.request().getPassenger().isVipLoungeAccess());
    }

    // ---------------------------------------------------------
    // Regla 10: PreferentialSeatForFamilies
    // ---------------------------------------------------------

    @Test
    void preferentialSeatForFamilyWithoutSeatPreference() {
        Passenger family = new Passenger("Familia Test", 35, "Basic", "Any", true);
        Seat familySeat = new Seat("F1", Seat.FAMILY);

        Evaluation result = evaluate(family, neutralFlight(), neutralLuggage(), familySeat);

        assertEquals("F1", result.request().getPassenger().getAssignedSeat());
        assertTrue(familySeat.isOccupied());
    }

    @Test
    void preferentialSeatWhenPreferenceIsNull() {
        Passenger family = new Passenger("Familia Test", 35, "Basic", null, true);
        Seat familySeat = new Seat("F2", Seat.FAMILY);

        Evaluation result = evaluate(family, neutralFlight(), neutralLuggage(), familySeat);

        assertEquals("F2", result.request().getPassenger().getAssignedSeat());
        assertTrue(familySeat.isOccupied());
    }

    @Test
    void noPreferentialSeatForPassengersWithoutChildren() {
        Passenger noChildren = new Passenger("Sin niños", 35, "Basic", "Any", false);
        Seat familySeat = new Seat("F3", Seat.FAMILY);

        Evaluation result = evaluate(noChildren, neutralFlight(), neutralLuggage(), familySeat);

        assertNull(result.request().getPassenger().getAssignedSeat());
        assertFalse(familySeat.isOccupied());
    }
}
