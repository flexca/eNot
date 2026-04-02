package com.github.flexca.enot.core.types.asn1.converter;

import com.github.flexca.enot.core.exception.EnotDataConvertingException;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.registry.EnotBinaryConverter;
import com.github.flexca.enot.core.types.asn1.Asn1EnotValueType;
import org.bouncycastle.asn1.ASN1Encodable;

public class Asn1ElementToBinaryConverter implements EnotBinaryConverter {

    @Override
    public byte[] toBinary(Object input) {

        if (input == null) {
            return null;
        }
        if (input instanceof ASN1Encodable asn1Encodable) {
            try {
                return asn1Encodable.toASN1Primitive().getEncoded();
            } catch (Exception e) {
                throw new EnotDataConvertingException("failure during converting of ASN.1 element to binary, reason: "
                        + e.getMessage(), e);
            }
        } else {
            throw new EnotInvalidArgumentException("expecting input of type " + Asn1EnotValueType.ASN1_ELEMENT.getName());
        }
    }
}
