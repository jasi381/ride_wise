package com.airtribe.parkinglot.api;

import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.repository.JdbcTransactionRepository;
import com.airtribe.parkinglot.repository.TransactionRepository;
import com.airtribe.parkinglot.service.ParkingLotLayout;
import com.airtribe.parkinglot.service.ParkingService;
import com.airtribe.parkinglot.strategy.BestFitSpotAllocation;
import com.airtribe.parkinglot.strategy.FeeStrategy;
import com.airtribe.parkinglot.strategy.HourlyFeeStrategy;
import com.airtribe.parkinglot.strategy.PeakHourFeeStrategy;
import com.airtribe.parkinglot.strategy.RateCard;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/** Wires the plain-Java parking classes into Spring. The domain classes stay framework-free. */
@Configuration
public class ParkingConfig {
    private static final Duration GRACE_PERIOD = Duration.ofMinutes(15);

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    FeeStrategy feeStrategy(@Value("${parking.fee-strategy:hourly}") String name, Clock clock) {
        RateCard rates = RateCard.standard();
        return switch (name) {
            case "hourly" -> new HourlyFeeStrategy(rates, GRACE_PERIOD);
            case "peak" -> new PeakHourFeeStrategy(rates, GRACE_PERIOD, LocalTime.of(8, 0), LocalTime.of(11, 0),
                    BigDecimal.valueOf(1.5), clock.getZone());
            default -> throw new IllegalArgumentException("Unknown parking.fee-strategy '" + name + "'. Use hourly or peak");
        };
    }

    @Bean
    TransactionRepository transactionRepository(JdbcTemplate jdbc) {
        return new JdbcTransactionRepository(jdbc);
    }

    @Bean
    ParkingService parkingService(FeeStrategy fees, TransactionRepository transactions, Clock clock) {
        BestFitSpotAllocation spots = new BestFitSpotAllocation(new ParkingLotLayout()
                .floor(0).spots(SpotSize.SMALL, 4).spots(SpotSize.MEDIUM, 4)
                .floor(1).spots(SpotSize.MEDIUM, 6).spots(SpotSize.LARGE, 2)
                .build());
        // Tickets that were still active at shutdown keep their spots after a restart.
        transactions.findActive().forEach(ticket -> spots.claim(ticket.spot()));
        // Random ids, because a counter would restart at 1 and collide with stored tickets.
        Supplier<String> ticketIds = () -> "T" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new ParkingService(spots, fees, transactions, clock, ticketIds);
    }
}
