package io.github.flexca.enot.bertlv.model;

import io.github.flexca.enot.bertlv.util.BerTlvUtils;

import java.io.IOException;
import java.io.OutputStream;

public class BerTlvLeafElement extends BerTlvElement<byte[]> {

    private byte[] value;

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public int getTotalLength() {
        int length = getTag().length;
        length += BerTlvUtils.calculateLength(value.length, isIndefiniteForm()).length;
        length += getValueLength();
        if (isIndefiniteForm()) {
            length += 2;
        }
        return length;
    }

    @Override
    public int getValueLength() {
        return value == null ? 0 : value.length;
    }

    @Override
    public void encodeToStream(OutputStream out) throws IOException {
        out.write(getTag());
        if (value == null) {
            out.write(0x0);
            return;
        }
        out.write(BerTlvUtils.calculateLength(value.length, isIndefiniteForm()));
        out.write(value);
        if (isIndefiniteForm()) {
            out.write(0x00);
            out.write(0x00);
        }
    }
}
