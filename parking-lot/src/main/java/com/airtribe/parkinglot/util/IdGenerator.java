package com.airtribe.parkinglot.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public final class IdGenerator implements Supplier<String> {
    private final String prefix;
    private final AtomicLong counter = new AtomicLong();

    public IdGenerator(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String get() {
        return prefix + counter.incrementAndGet();
    }
}
