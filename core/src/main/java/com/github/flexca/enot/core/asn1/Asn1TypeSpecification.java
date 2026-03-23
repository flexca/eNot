package com.github.flexca.enot.core.asn1;

import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.asn1.validation.Asn1ElementValidator;
import com.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;

public class Asn1TypeSpecification implements EnotTypeSpecification {

    public static final String TYPE_NAME = "asn.1";

    private final Asn1ElementValidator asn1ElementValidator = new Asn1ElementValidator();

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public EnotAttribute resolveAttributeByName(String name) {
        return Asn1Attribute.fromJsonString(name);
    }

    @Override
    public EnotElementSpecification getElementSpecification(EnotElement element) {
        Object tagObject = element.getAttributes().get(Asn1Attribute.TAG);
        if (tagObject instanceof String tagString) {
            Asn1Tag tag = Asn1Tag.fromString(tagString);
            if (tag != null) {
                return tag;
            }
        }
        throw new EnotInvalidConfigurationException("Invalid or missing tag attribute for ASN.1 element");
    }

    @Override
    public EnotElementValidator getElementValidator() {
        return asn1ElementValidator;
    }
}
