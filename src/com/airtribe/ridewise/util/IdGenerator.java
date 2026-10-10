package com.airtribe.ridewise.util;

public final class IdGenerator {
    private final String prefix;
    private long sequence = 0;

    public IdGenerator(String prefix) { this.prefix = prefix; }

    public String nextId() {
        sequence++;
        return prefix + sequence;
    }
}
