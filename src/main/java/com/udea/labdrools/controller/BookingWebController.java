package com.udea.labdrools.controller;

import com.udea.labdrools.model.BookingRequest;
import com.udea.labdrools.model.BookingResponse;
import com.udea.labdrools.model.Flight;
import com.udea.labdrools.model.Luggage;
import com.udea.labdrools.model.Passenger;
import com.udea.labdrools.model.Seat;
import com.udea.labdrools.service.BookingEvaluationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador MVC (vistas Thymeleaf) de la reserva de vuelo.
 *
 * Endpoints:
 * - GET  /booking/form     -> formulario con los datos del pasajero, vuelo,
 *                             equipaje, boleto y asientos disponibles.
 * - POST /booking/evaluate -> evaluación con Drools y página de resultado.
 *
 * El controlador REST (/booking/api/evaluate) sigue existiendo por separado
 * en BookingEvaluationController.
 */
@Controller
@RequestMapping("/booking")
public class BookingWebController {

    @Autowired
    private BookingEvaluationService evaluationService;

    // Muestra el formulario inicial con los objetos anidados vacíos y dos asientos de ejemplo
    @GetMapping("/form")
    public String showForm(Model model) {
        if (!model.containsAttribute("bookingRequest")) {
            BookingRequest request = new BookingRequest();
            request.setPassenger(new Passenger());
            request.setFlight(new Flight());
            request.setLuggage(new Luggage());
            // Dos asientos de ejemplo para que participen las reglas 5 (salida de emergencia) y 10 (familias)
            List<Seat> seats = new ArrayList<>();
            seats.add(new Seat("12A", Seat.EMERGENCY_EXIT));
            seats.add(new Seat("14C", Seat.FAMILY));
            request.setAvailableSeats(seats);
            model.addAttribute("bookingRequest", request);
        }
        return "booking_form";
    }

    // Evalúa la solicitud enviada por el formulario y muestra el resultado
    @PostMapping("/evaluate")
    public String evaluate(@Valid @ModelAttribute("bookingRequest") BookingRequest request,
                           BindingResult bindingResult, Model model) {
        // Si hay errores de validación se vuelve a mostrar el formulario con los mensajes
        if (bindingResult.hasErrors()) {
            return "booking_form";
        }
        BookingResponse response = evaluationService.evaluateBooking(request, request.getAvailableSeats());
        model.addAttribute("bookingResponse", response);
        // Se envía también la solicitud para mostrar los estados finales que actualizaron las reglas
        model.addAttribute("bookingRequest", request);
        return "booking_result";
    }
}
