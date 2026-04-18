package com.github.flexca.enot.core.registry;

public class EnotElementPathAltering {

    private static final EnotElementPathAltering NONE = new EnotElementPathAltering(null, EnotElementPathAlteringType.NONE);

    private final String key;
    private final EnotElementPathAlteringType type;

    private EnotElementPathAltering(String key, EnotElementPathAlteringType type) {
        this.key = key;
        this.type = type;
    }

    public static EnotElementPathAltering none() {
        return NONE;
    }

    public static EnotElementPathAltering arrayScope(String key) {
        return new EnotElementPathAltering(key, EnotElementPathAlteringType.ARRAY_SCOPE);
    }

    public static EnotElementPathAltering mapScope(String key) {
        return new EnotElementPathAltering(key, EnotElementPathAlteringType.MAP_SCOPE);
    }

    public String getKey() {
        return key;
    }

    public EnotElementPathAlteringType getType() {
        return type;
    }
}
