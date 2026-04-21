package com.github.flexca.enot.bertlv.model;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BerTlvElement<T> {

    private byte[] tag;
    private boolean indefiniteForm = false;

    public abstract boolean isLeaf();

    public abstract T getValue();

    public abstract void setValue(T value);

    public abstract int getTotalLength();

    public abstract int getValueLength();

    public abstract void encodeToStream(OutputStream out) throws IOException;

    public byte[] getTag() {
        return tag;
    }

    public void setTag(byte[] tag) {
        this.tag = tag;
    }

    public boolean isIndefiniteForm() {
        return indefiniteForm;
    }

    public void setIndefiniteForm(boolean indefiniteForm) {
        this.indefiniteForm = indefiniteForm;
    }
}
