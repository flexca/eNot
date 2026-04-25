package io.github.flexca.enot.core.expression.model;

public abstract class ExpressionBlock {

    private final boolean inverted;
    private final boolean leaf;

    protected ExpressionBlock(boolean inverted, boolean leaf) {
        this.inverted = inverted;
        this.leaf = leaf;
    }

    public abstract ExpressionBlock invert();

    public boolean isInverted() {
        return inverted;
    }

    public boolean isLeaf() {
        return leaf;
    }

}
