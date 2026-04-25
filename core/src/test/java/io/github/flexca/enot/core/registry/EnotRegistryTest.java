package io.github.flexca.enot.core.registry;

import org.junit.jupiter.api.Test;

public class EnotRegistryTest {

    private EnotRegistry enotRegistry;

    @Test
    void testBuilder() throws Exception {
        EnotRegistry enotRegistry = new EnotRegistry.Builder().build();
    }
}
