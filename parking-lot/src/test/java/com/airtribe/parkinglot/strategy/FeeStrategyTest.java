package com.airtribe.parkinglot.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.airtribe.parkinglot.model.VehicleType;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class FeeStrategyTest {
    private static final Instant NOON = Instant.parse("2026-01-05T12:00:00Z");
    private final HourlyFeeStrategy hourly = new HourlyFeeStrategy(RateCard.standard(), Duration.ofMinutes(15));

    private BigDecimal fee(FeeStrategy strategy, VehicleType type, Instant entry, Duration stay) {
        return strategy.calculate(type, entry, entry.plus(stay));
    }

    private void assertFee(int expected, BigDecimal actual) {
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(actual), "fee was " + actual);
    }

    @Test
    void stayWithinGracePeriodIsFree() {
        assertFee(0, fee(hourly, VehicleType.CAR, NOON, Duration.ofMinutes(15)));
    }

    @Test
    void firstMinutePastGraceBillsOneFullHour() {
        assertFee(20, fee(hourly, VehicleType.CAR, NOON, Duration.ofMinutes(16)));
    }

    @Test
    void partialHoursRoundUp() {
        assertFee(60, fee(hourly, VehicleType.CAR, NOON, Duration.ofMinutes(121)));
    }

    @Test
    void exactHoursAreNotRoundedUp() {
        assertFee(40, fee(hourly, VehicleType.CAR, NOON, Duration.ofHours(2)));
    }

    @Test
    void secondsPastTheHourStartAnotherHour() {
        assertFee(40, fee(hourly, VehicleType.CAR, NOON, Duration.ofSeconds(3601)));
    }

    @Test
    void vehicleTypeChangesTheFare() {
        Duration threeHours = Duration.ofHours(3);
        assertFee(30, fee(hourly, VehicleType.MOTORCYCLE, NOON, threeHours));
        assertFee(60, fee(hourly, VehicleType.CAR, NOON, threeHours));
        assertFee(150, fee(hourly, VehicleType.BUS, NOON, threeHours));
    }

    @Test
    void exitBeforeEntryIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> hourly.calculate(VehicleType.CAR, NOON, NOON.minusSeconds(1)));
    }

    @Test
    void rateCardRequiresARateForEveryType() {
        assertThrows(IllegalArgumentException.class,
                () -> new RateCard(java.util.Map.of(VehicleType.CAR, BigDecimal.TEN)));
    }

    private PeakHourFeeStrategy peak(LocalTime start, LocalTime end) {
        return new PeakHourFeeStrategy(RateCard.standard(), Duration.ofMinutes(15), start, end,
                new BigDecimal("1.5"), ZoneOffset.UTC);
    }

    @Test
    void peakMultiplierOnlyAppliesInsideTheWindow() {
        PeakHourFeeStrategy strategy = peak(LocalTime.of(8, 0), LocalTime.of(11, 0));
        Instant offPeak = Instant.parse("2026-01-05T12:00:00Z");
        Instant inPeak = Instant.parse("2026-01-05T09:00:00Z");
        assertFee(20, fee(strategy, VehicleType.CAR, offPeak, Duration.ofHours(1)));
        assertFee(30, fee(strategy, VehicleType.CAR, inPeak, Duration.ofHours(1)));
    }

    @Test
    void stayCrossingIntoPeakPaysPeakOnlyForPeakHours() {
        PeakHourFeeStrategy strategy = peak(LocalTime.of(8, 0), LocalTime.of(11, 0));
        Instant entry = Instant.parse("2026-01-05T07:30:00Z"); // hours start 07:30 (off), 08:30 (peak)
        assertFee(50, fee(strategy, VehicleType.CAR, entry, Duration.ofHours(2)));
    }

    @Test
    void peakWindowCanWrapPastMidnight() {
        PeakHourFeeStrategy strategy = peak(LocalTime.of(22, 0), LocalTime.of(2, 0));
        assertFee(30, fee(strategy, VehicleType.CAR, Instant.parse("2026-01-05T23:00:00Z"), Duration.ofHours(1)));
        assertFee(30, fee(strategy, VehicleType.CAR, Instant.parse("2026-01-05T01:00:00Z"), Duration.ofHours(1)));
        assertFee(20, fee(strategy, VehicleType.CAR, Instant.parse("2026-01-05T03:00:00Z"), Duration.ofHours(1)));
    }

    @Test
    void peakStrategyKeepsTheGracePeriod() {
        PeakHourFeeStrategy strategy = peak(LocalTime.of(0, 0), LocalTime.of(23, 59));
        assertFee(0, fee(strategy, VehicleType.BUS, NOON, Duration.ofMinutes(10)));
    }
}
