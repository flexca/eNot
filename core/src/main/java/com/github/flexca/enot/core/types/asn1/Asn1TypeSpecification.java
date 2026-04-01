package com.github.flexca.enot.core.types.asn1;

import com.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.types.asn1.validation.Asn1ElementValidator;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.element.attribute.EnotAttribute;
import com.github.flexca.enot.core.element.value.EnotValueType;
import com.github.flexca.enot.core.serializer.ElementSerializer;

import java.util.List;
import java.util.Optional;

public class Asn1TypeSpecification implements EnotTypeSpecification {

    public static final String TYPE_NAME = "asn.1";

    private final Asn1ElementValidator asn1ElementValidator = new Asn1ElementValidator();

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public List<EnotValueType> getValueTypes() {
        return List.of(Asn1EnotValueType.values());
    }

    @Override
    public List<EnotAttribute> getAttributes() {
        return List.of(Asn1Attribute.values());
    }

    @Override
    public EnotAttribute resolveAttributeByName(String name) {
        return Asn1Attribute.fromJsonString(name);
    }

    @Override
    public EnotElementSpecification getElementSpecification(EnotElement element) {
        return getAsn1TagForElement(element);
    }

    @Override
    public EnotElementValidator getElementValidator() {
        return asn1ElementValidator;
    }

    @Override
    public ElementSerializer getSerializer(EnotElement element) {
        Asn1Tag tag = getAsn1TagForElement(element);
        if (tag == null) {
            return null;
        }
        return tag.getElementSerializer();
    }

    private Asn1Tag getAsn1TagForElement(EnotElement element) {
        Object tagObject = element.getAttributes().get(Asn1Attribute.TAG);
        if (tagObject instanceof String tagString) {
            Asn1Tag tag = Asn1Tag.fromString(tagString);
            if (tag != null) {
                return tag;
            }
        }
        return null;
    }
}
