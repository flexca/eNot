package io.github.flexca.enot.bertlv.converter;

import io.github.flexca.enot.bertlv.BerTlvValueType;
import io.github.flexca.enot.bertlv.model.BerTlvElement;
import io.github.flexca.enot.core.exception.EnotDataConvertingException;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;

import java.io.ByteArrayOutputStream;

/**
 * Converts a {@link BerTlvElement} model object to its binary TLV encoding.
 * <p>
 * This converter is the final step in the BER-TLV serialization pipeline. It is registered
 * as the binary converter for {@link BerTlvValueType#BER_TLV_ELEMENT} and is invoked by the
 * eNot framework whenever a BER-TLV element result needs to be turned into raw bytes.
 * <p>
 * Encoding is delegated to {@link BerTlvElement#encodeToStream(java.io.OutputStream)},
 * which recursively encodes the full TLV tree.
 */
public class BerTlvElementToBinaryConverter implements EnotBinaryConverter {

    @Override
    public byte[] toBinary(Object input) {

        if (input == null) {
            return null;
        }
        if (input instanceof BerTlvElement<?> berTlvElement) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                berTlvElement.encodeToStream(out);
                return out.toByteArray();
            } catch (Exception e) {
                throw new EnotDataConvertingException("failure during converting of BER-TLV element to binary, reason: "
                        + e.getMessage(), e);
            }
        } else {
            throw new EnotInvalidArgumentException("expecting input of type " + BerTlvValueType.BER_TLV_ELEMENT.getName());
        }
    }
}
