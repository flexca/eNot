package io.github.flexca.enot.bertlv.model;

import io.github.flexca.enot.bertlv.util.BerTlvUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * A constructed (node) BER-TLV element whose value is a list of nested child
 * {@link BerTlvElement} instances.
 * <p>
 * Encoding layout (definite form): {@code <tag> <total-child-length> <child1> <child2> ...}<br>
 * Encoding layout (indefinite form): {@code <tag> 80 <child1> <child2> ... 00 00}
 * <p>
 * Example — tag {@code 73} containing a {@link BerTlvLeafElement} with tag {@code 04}, value {@code AA BB}:
 * <pre>{@code
 * 73 04 04 02 AA BB
 * }</pre>
 * Children are encoded recursively, supporting arbitrary nesting depth.
 */
public class BerTlvNodeElement extends BerTlvElement<List<BerTlvElement<?>>> {

    private List<BerTlvElement<?>> value;

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public List<BerTlvElement<?>> getValue() {
        return value;
    }

    @Override
    public void setValue(List<BerTlvElement<?>> value) {
        this.value = value;
    }

    @Override
    public int getTotalLength() {
        int valueLength = getValueLength();
        int length = getTag().length;
        length += BerTlvUtils.calculateLength(valueLength, isIndefiniteForm()).length;
        length += valueLength;
        if (isIndefiniteForm()) {
            length += 2;
        }
        return length;
    }

    @Override
    public int getValueLength() {
        int valueLength = 0;
        for (BerTlvElement<?> element : value) {
            valueLength += element.getTotalLength();
        }
        return valueLength;
    }

    @Override
    public void encodeToStream(OutputStream out) throws IOException {
        out.write(getTag());
        if (CollectionUtils.isEmpty(value)) {
            out.write(0x0);
            return;
        }
        out.write(BerTlvUtils.calculateLength(getValueLength(), isIndefiniteForm()));
        for (BerTlvElement<?> element : value) {
            element.encodeToStream(out);
        }
        if (isIndefiniteForm()) {
            out.write(0x00);
            out.write(0x00);
        }
    }
}
