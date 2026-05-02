package io.github.flexca.enot.core.registry;

/**
 * Enumerates the ways an {@link EnotElementPathAltering} can move the serializer's
 * parameter-tree cursor before its children are processed.
 *
 * <p>This is an internal enum used by the serialization pipeline. Custom element
 * type implementations do not need to reference it directly — use the factory methods
 * on {@link EnotElementPathAltering} instead.
 */
public enum EnotElementPathAlteringType {

    /** No movement — children are serialized against the current parameter node. */
    NONE,

    /**
     * The cursor steps into a named array child and the serializer iterates over
     * its items. Used by {@code system/loop}.
     */
    ARRAY_SCOPE,

    /**
     * The cursor steps into a named map child, scoping placeholder lookups to
     * that sub-object. Used by {@code system/group}.
     */
    MAP_SCOPE;
}
