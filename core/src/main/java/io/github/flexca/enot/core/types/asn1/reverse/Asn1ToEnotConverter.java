package io.github.flexca.enot.core.types.asn1.reverse;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.exception.EnotRuntimeException;
import io.github.flexca.enot.core.types.asn1.Asn1Tag;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import io.github.flexca.enot.core.types.system.SystemKind;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.util.DateTimeUtils;
import org.bouncycastle.asn1.ASN1BMPString;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.ASN1VisibleString;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class Asn1ToEnotConverter {

    public EnotElement toEnot(String asn1Base64Encoded) {

        byte[] asn1Binary;
        try {
            asn1Binary = Base64.getDecoder().decode(asn1Base64Encoded);
        } catch(Exception e) {
            throw new EnotInvalidArgumentException("failure during base64 decoding, reason: " + e.getMessage(), e);
        }
        return toEnot(asn1Binary);
    }

    public EnotElement toEnot(byte[] asn1Binary) {

        ASN1Encodable encodable;
        try {
            encodable = ASN1Primitive.fromByteArray(asn1Binary);
        } catch (Exception e) {
            throw new EnotInvalidArgumentException("failure during ASN.1 parsing, reason: " + e.getMessage(), e);
        }
        return toEnot(encodable);
    }

    public EnotElement toEnot(ASN1Encodable encodable) {

        try {
            if (encodable instanceof ASN1Sequence sequence) {
                return convertSequence(sequence);
            } else if (encodable instanceof ASN1Set set) {
                return convertSet(set);
            } else if (encodable instanceof ASN1OctetString octetString) {
                return convertOctetString(octetString);
            } else if (encodable instanceof ASN1TaggedObject taggedObject) {
                return convertTaggedObject(taggedObject);
            } else {
                return convertLeafTypes(encodable);
            }
        } catch (EnotRuntimeException enote) {
            throw enote;
        } catch (Exception e) {
            throw new EnotInvalidArgumentException("failure during ASN.1 to eNot conversion, reason: " + e.getMessage(), e);
        }
    }

    private EnotElement convertSequence(ASN1Sequence sequence) {

        EnotElement enotSequence = createBaseElement(Asn1Tag.SEQUENCE);
        List<EnotElement> body = new ArrayList<>();
        for (ASN1Encodable child : sequence.toArray()) {
            body.add(toEnot(child));
        }
        enotSequence.setBody(body);
        return enotSequence;
    }

    private EnotElement convertSet(ASN1Set set) {

        EnotElement enotSet = createBaseElement(Asn1Tag.SET);
        List<EnotElement> body = new ArrayList<>();
        for (ASN1Encodable child : set.toArray()) {
            body.add(toEnot(child));
        }
        enotSet.setBody(body);
        return enotSet;
    }

    private EnotElement convertOctetString(ASN1OctetString octetString) {

        EnotElement element = createBaseElement(Asn1Tag.OCTET_STRING);
        try {
            ASN1Encodable stringContent = ASN1Primitive.fromByteArray(octetString.getOctets());
            EnotElement subElement = toEnot(stringContent);
            element.setBody(subElement);
        } catch (Exception e) {
            EnotElement hexToBinElement = createBaseSystemElement(SystemKind.HEX_TO_BIN);
            hexToBinElement.setBody(HexFormat.of().formatHex(octetString.getOctets()));
            element.setBody(hexToBinElement);
        }
        return element;
    }

    private EnotElement convertTaggedObject(ASN1TaggedObject taggedObject) {

        EnotElement element = createBaseElement(Asn1Tag.TAGGED_OBJECT);
        if (taggedObject.isExplicit()) {
            element.getAttributes().put(Asn1Attribute.EXPLICIT, taggedObject.getTagNo());
        } else {
            element.getAttributes().put(Asn1Attribute.IMPLICIT, taggedObject.getTagNo());
        }
        element.setBody(toEnot(taggedObject.getBaseObject()));
        return element;
    }

    private EnotElement convertLeafTypes(ASN1Encodable encodable) {

        EnotElement element;
        if (encodable instanceof ASN1ObjectIdentifier objectIdentifier) {
            element = createBaseElement(Asn1Tag.OBJECT_IDENTIFIER);
            element.setBody(objectIdentifier.getId());
        } else if (encodable instanceof ASN1Boolean booleanValue) {
            element = createBaseElement(Asn1Tag.BOOLEAN);
            element.setBody(booleanValue.isTrue());
        } else if (encodable instanceof ASN1Integer integerValue) {
            element = createBaseElement(Asn1Tag.INTEGER);
            element.setBody(integerValue.getValue().toString(10));
        } else if (encodable instanceof ASN1PrintableString printableString) {
            element = createBaseElement(Asn1Tag.PRINTABLE_STRING);
            element.setBody(printableString.getString());
        } else if (encodable instanceof ASN1IA5String ia5String) {
            element = createBaseElement(Asn1Tag.IA5_STRING);
            element.setBody(ia5String.getString());
        } else if (encodable instanceof ASN1VisibleString visibleString) {
            element = createBaseElement(Asn1Tag.VISIBLE_STRING);
            element.setBody(visibleString.getString());
        } else if (encodable instanceof ASN1UTF8String utf8String) {
            element = createBaseElement(Asn1Tag.UTF8_STRING);
            element.setBody(utf8String.getString());
        } else if (encodable instanceof ASN1BMPString bmpString) {
            element = createBaseElement(Asn1Tag.BMP_STRING);
            element.setBody(bmpString.getString());
        } else if (encodable instanceof ASN1BitString bitString) {
            EnotElement hexToBinElement = createBaseSystemElement(SystemKind.HEX_TO_BIN);
            hexToBinElement.setBody(HexFormat.of().formatHex(bitString.getOctets()));
            element = createBaseElement(Asn1Tag.BIT_STRING);
            element.setBody(hexToBinElement);
        } else if (encodable instanceof ASN1GeneralizedTime generalizedTime) {
            Date date;
            try {
                date = generalizedTime.getDate();
            } catch (Exception e) {
                throw new EnotInvalidArgumentException("failed to extract date, reason: " + e.getMessage(), e);
            }
            ZonedDateTime dateTime = DateTimeUtils.toZonedDateTime(date);
            element = createBaseElement(Asn1Tag.GENERALIZED_TIME);
            element.setBody(DateTimeUtils.format(dateTime));
        } else if (encodable instanceof ASN1UTCTime utcTime) {
            Date date;
            try {
                date = utcTime.getDate();
            } catch (Exception e) {
                throw new EnotInvalidArgumentException("failed to extract date, reason: " + e.getMessage(), e);
            }
            ZonedDateTime dateTime = DateTimeUtils.toZonedDateTime(date);
            element = createBaseElement(Asn1Tag.UTC_TIME);
            element.setBody(DateTimeUtils.format(dateTime));
        } else if (encodable instanceof ASN1Null) {
            element = createBaseElement(Asn1Tag.NULL);
        } else {
            throw new EnotInvalidArgumentException("unsupported ASN.1 tag " + encodable.getClass());
        }
        return element;
    }

    private EnotElement createBaseElement(Asn1Tag tag) {

        EnotElement element = new EnotElement();
        element.setType(Asn1TypeSpecification.TYPE_NAME);
        element.setOptional(false);
        Map<EnotAttribute, Object> attributes = new HashMap<>();
        attributes.put(Asn1Attribute.TAG, tag.getName());
        element.setAttributes(attributes);
        return element;
    }

    private EnotElement createBaseSystemElement(SystemKind kind) {

        EnotElement element = new EnotElement();
        element.setType(SystemTypeSpecification.TYPE_NAME);
        element.setOptional(false);
        Map<EnotAttribute, Object> attributes = new HashMap<>();
        attributes.put(SystemAttribute.KIND, kind.getName());
        element.setAttributes(attributes);
        return element;
    }
}
