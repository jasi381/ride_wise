package com.airtribe.parkinglot.api;

import com.airtribe.parkinglot.model.ParkingTransaction;
import com.airtribe.parkinglot.model.Receipt;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.Vehicle;
import com.airtribe.parkinglot.service.ParkingService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ParkingController {
    private final ParkingService parking;

    public ParkingController(ParkingService parking) {
        this.parking = parking;
    }

    /** Body: {"plate": "KA01AB1234", "type": "CAR"}. */
    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingTransaction checkIn(@RequestBody Vehicle vehicle) {
        return parking.checkIn(vehicle);
    }

    @PostMapping("/tickets/{ticketId}/checkout")
    public Receipt checkOut(@PathVariable String ticketId) {
        return parking.checkOut(ticketId);
    }

    @GetMapping("/availability")
    public Map<SpotSize, Integer> availability() {
        return parking.availability();
    }

    @GetMapping("/tickets")
    public List<ParkingTransaction> active() {
        return parking.activeTickets();
    }

    @GetMapping("/tickets/history")
    public List<ParkingTransaction> history() {
        return parking.history();
    }
}
