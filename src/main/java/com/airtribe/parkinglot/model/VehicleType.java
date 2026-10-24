package com.airtribe.parkinglot.model;

/** Vehicle classes the lot accepts. The type decides both which spots fit and which rate applies. */
public enum VehicleType {
    MOTORCYCLE(SpotSize.SMALL),
    CAR(SpotSize.MEDIUM),
    BUS(SpotSize.LARGE);

    private final SpotSize minimumSpotSize;

    VehicleType(SpotSize minimumSpotSize) {
        this.minimumSpotSize = minimumSpotSize;
    }

    public SpotSize minimumSpotSize() {
        return minimumSpotSize;
    }

    /** A spot fits when it is at least as big as the vehicle needs. */
    public boolean fitsIn(SpotSize size) {
        return size.ordinal() >= minimumSpotSize.ordinal();
    }
}
