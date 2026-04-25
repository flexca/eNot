package io.github.flexca.enot.bertlv.converter;

import io.github.flexca.enot.bertlv.BerTlvValueType;
import io.github.flexca.enot.bertlv.model.BerTlvElement;
import io.github.flexca.enot.core.exception.EnotDataConvertingException;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;

import java.io.ByteArrayOutputStream;

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
