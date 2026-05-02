package io.github.flexca.enot.core.registry;

/**
 * Describes how the serializer should move the parameter-tree cursor before
 * serializing an element's children.
 *
 * <p>This is an internal mechanism used by the eNot serialization pipeline to
 * navigate the {@link io.github.flexca.enot.core.serializer.context.SerializationContext}
 * parameter tree. It is not part of the public extension API — custom element
 * type implementations do not need to create or interpret {@code EnotElementPathAltering}
 * instances directly.
 *
 * <p>The three possible behaviours, selected by {@link EnotElementPathAlteringType}, are:
 * <ul>
 *   <li>{@link EnotElementPathAlteringType#NONE} — the cursor does not move; children
 *       are serialized against the same parameter node as the parent. Returned by
 *       {@link #none()}. This is the default for all element types.</li>
 *   <li>{@link EnotElementPathAlteringType#ARRAY_SCOPE} — the cursor steps into a named
 *       array child and advances through its items one by one. Used by the
 *       {@code system/loop} element. Returned by {@link #arrayScope(String)}.</li>
 *   <li>{@link EnotElementPathAlteringType#MAP_SCOPE} — the cursor steps into a named
 *       map child, making its fields available to nested placeholders. Used by the
 *       {@code system/group} element. Returned by {@link #mapScope(String)}.</li>
 * </ul>
 *
 * <p>{@code EnotTypeSpecification} implementations that need non-default path behaviour
 * should override {@link EnotTypeSpecification#getPathAltering(io.github.flexca.enot.core.element.EnotElement)}
 * and return an appropriate instance.
 */
public class EnotElementPathAltering {

    private static final EnotElementPathAltering NONE = new EnotElementPathAltering(null, EnotElementPathAlteringType.NONE);

    private final String key;
    private final EnotElementPathAlteringType type;

    private EnotElementPathAltering(String key, EnotElementPathAlteringType type) {
        this.key = key;
        this.type = type;
    }

    /**
     * Returns the singleton instance representing no path movement.
     * This is the correct return value for the vast majority of element types.
     *
     * @return a shared, immutable no-op instance
     */
    public static EnotElementPathAltering none() {
        return NONE;
    }

    /**
     * Returns an instance that moves the cursor into the named array child,
     * enabling iteration over its items.
     *
     * @param key the attribute key whose value identifies the array parameter name
     * @return a new path-altering instance of type {@link EnotElementPathAlteringType#ARRAY_SCOPE}
     */
    public static EnotElementPathAltering arrayScope(String key) {
        return new EnotElementPathAltering(key, EnotElementPathAlteringType.ARRAY_SCOPE);
    }

    /**
     * Returns an instance that moves the cursor into the named map child,
     * making its fields available to nested placeholder lookups.
     *
     * @param key the attribute key whose value identifies the map parameter name
     * @return a new path-altering instance of type {@link EnotElementPathAlteringType#MAP_SCOPE}
     */
    public static EnotElementPathAltering mapScope(String key) {
        return new EnotElementPathAltering(key, EnotElementPathAlteringType.MAP_SCOPE);
    }

    /** Returns the parameter key used to locate the child node to step into. */
    public String getKey() {
        return key;
    }

    /** Returns the type of path movement this instance describes. */
    public EnotElementPathAlteringType getType() {
        return type;
    }
}
