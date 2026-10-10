package com.airtribe.ridewise.util;

import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {
    private final String prefix;
    private final AtomicLong sequence = new AtomicLong();

    public IdGenerator(String prefix) { this.prefix = prefix; }

    public String nextId() { return prefix + sequence.incrementAndGet(); }
}
