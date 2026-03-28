package com.github.flexca.enot.core.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnotRegistryTest {

    private EnotRegistry enotRegistry;

    @Test
    void testConstructor() throws Exception {
        EnotRegistry enotRegistry = new EnotRegistry();
    }

    @Test
    void testBuilder() throws Exception {
        EnotRegistry enotRegistry = new EnotRegistry.Builder().build();
    }
}
